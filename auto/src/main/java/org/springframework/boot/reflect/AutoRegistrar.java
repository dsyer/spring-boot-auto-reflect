/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class AutoRegistrar {

	private final ConditionEvaluator evaluator;
	private final BeanDefinitionRegistry registry;
	private final ConfigurableListableBeanFactory beanFactory;

	public AutoRegistrar(BeanDefinitionRegistry registry, ConfigurableListableBeanFactory beanFactory, Environment environment,
			ResourceLoader resourceLoader) {
		this.registry = registry;
		this.beanFactory = beanFactory;
		this.evaluator = new ConditionEvaluator(registry, environment, resourceLoader);
	}

	public void register(Class<?> type) {
		try {
			StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(
					type);
			if (evaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
				return;
			}
			register(registry, evaluator, type, metadata);
		}
		catch (ArrayStoreException e) {
			// ignore
		}
	}


	private void register(BeanDefinitionRegistry registry, ConditionEvaluator evaluator,
			Class<?> type, StandardAnnotationMetadata metadata) {
		for (Class<?> nested : type.getDeclaredClasses()) {
			if (Modifier.isStatic(nested.getModifiers())) {
				try {
					if (!registry.containsBeanDefinition(nested.getName())) {
						StandardAnnotationMetadata nestedMetadata = new StandardAnnotationMetadata(
								nested);
						if (nestedMetadata.hasAnnotation(Configuration.class.getName())
								|| nestedMetadata
										.hasAnnotatedMethods(Bean.class.getName())) {
							if (!evaluator.shouldSkip(nestedMetadata,
									ConfigurationPhase.REGISTER_BEAN)) {
								register(registry, evaluator, nested, nestedMetadata);
							}
						}
					}
				}
				catch (ArrayStoreException e) {
					// TODO: use ASM to avoid this?
				}
			}
		}
		if (metadata.hasAnnotation(Import.class.getName())) {
			Object[] props = (Object[]) metadata
					.getAnnotationAttributes(Import.class.getName()).get("value");
			if (props != null && props.length > 0) {
				for (Object object : props) {
					Class<?> imported = (Class<?>) object;
					if (ImportBeanDefinitionRegistrar.class.isAssignableFrom(imported)) {
						ImportBeanDefinitionRegistrar registrar = (ImportBeanDefinitionRegistrar) this.beanFactory
								.createBean(imported);
						registrar.registerBeanDefinitions(metadata, registry);
					}
					try {
						StandardAnnotationMetadata nestedMetadata = new StandardAnnotationMetadata(
								imported);
						if (!registry.containsBeanDefinition(imported.getName())
								&& !evaluator.shouldSkip(nestedMetadata,
										ConfigurationPhase.REGISTER_BEAN)) {
							register(registry, evaluator, imported, nestedMetadata);
						}
					}
					catch (ArrayStoreException e) {
						// TODO: use ASM to avoid this?
					}
				}
			}
		}
		if (metadata.hasAnnotation(EnableConfigurationProperties.class.getName())) {
			Object[] props = (Object[]) metadata.getAnnotationAttributes(
					EnableConfigurationProperties.class.getName()).get("value");
			if (props != null && props.length > 0) {
				for (Object object : props) {
					Class<?> prop = (Class<?>) object;
					String name = prop.getName();
					if (!registry.containsBeanDefinition(name)) {
						registry.registerBeanDefinition(name, BeanDefinitionBuilder
								.genericBeanDefinition(prop).getRawBeanDefinition());
					}
				}
			}
		}
		registry.registerBeanDefinition(type.getName(),
				BeanDefinitionBuilder.genericBeanDefinition(type).getRawBeanDefinition());
		Set<MethodMetadata> methods = metadata.getAnnotatedMethods(Bean.class.getName());
		Map<String, MethodMetadata> beans = new HashMap<>();
		for (MethodMetadata method : methods) {
			beans.put(method.getMethodName(), method);
		}
		for (Method method : ReflectionUtils.getUniqueDeclaredMethods(type)) {
			if (AnnotationUtils.findAnnotation(method, Bean.class) != null) {
				register(registry, evaluator, type, method, beans.get(method.getName()));
			}
		}
	}

	private void register(BeanDefinitionRegistry registry, ConditionEvaluator evaluator,
			Class<?> type, Method method, MethodMetadata metadata) {
		try {
			if (!evaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
				Class<?> beanClass = method.getReturnType();
				Supplier<?> supplier = () -> {
					Object[] args = params(method, this.beanFactory);
					ReflectionUtils.makeAccessible(method);
					Object result = ReflectionUtils.invokeMethod(method,
							getBean(method, type), args);
					return result;
				};
				RootBeanDefinition definition = new RootBeanDefinition();
				definition.setTargetType(beanClass);
				definition.setInstanceSupplier(supplier);
				definition.setFactoryMethodName(method.getName());
				// Bean name for factory...
				definition.setFactoryBeanName(type.getName());
				registry.registerBeanDefinition(method.getName(), definition);
			}
		}
		catch (ArrayStoreException e) {
			// TODO: use ASM to avoid this?
		}
	}

	private Object getBean(Method method, Class<?> type) {
		if (Modifier.isStatic(method.getModifiers())) {
			return null;
		}
		// We have to use getBeansOfType() to avoid eager instantiation of everything when
		// this is a factory for a bean factory post processor
		Map<String, ?> beans = this.beanFactory.getBeansOfType(type, false, false);
		// TODO: deal with no unique bean
		return beans.values().iterator().next();
	}

	private Object[] params(Method method, ConfigurableListableBeanFactory factory) {
		Object[] params = new Object[method.getParameterCount()];
		for (int i = 0; i < params.length; i++) {
			// TODO: deal with required flag
			params[i] = factory.resolveDependency(
					new DependencyDescriptor(new MethodParameter(method, i), false),
					method.getName());
		}
		return params;
	}
}
