/*
 * Copyright 2016-2017 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.reflect.AutoConfigurations.EnableActuatorAutoConfigurations;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.ClassUtils;

@Configuration
@EnableActuatorAutoConfigurations
class AutoConfigurations extends AutoConfigurationImportSelector
		implements BeanDefinitionRegistryPostProcessor {

	private GenericApplicationContext context;

	public AutoConfigurations(GenericApplicationContext applicationContext) {
		this.context = applicationContext;
		setBeanFactory(applicationContext.getDefaultListableBeanFactory());
		setBeanClassLoader(applicationContext.getClassLoader());
		setEnvironment(applicationContext.getEnvironment());
		setResourceLoader(applicationContext);
	}

	public Class<?>[] config() {
		String[] imports = selectImports(
				new StandardAnnotationMetadata(AutoConfigurations.class));
		Class<?>[] types = new Class<?>[imports.length];
		int i = 0;
		for (String config : imports) {
			Class<?> type = ClassUtils.resolveClassName(config, getBeanClassLoader());
			types[i++] = type;
		}
		org.springframework.boot.autoconfigure.AutoConfigurations autos = org.springframework.boot.autoconfigure.AutoConfigurations
				.of(types);
		return org.springframework.boot.autoconfigure.AutoConfigurations
				.getClasses(autos);
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Inherited
	@AutoConfigurationPackage
	static @interface EnableActuatorAutoConfigurations {
		Class<?>[] exclude() default {};

		String[] excludeName() default {};
	}

	@Override
	protected Class<?> getAnnotationClass() {
		return EnableActuatorAutoConfigurations.class;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		try {
			register(registry, this.context.getDefaultListableBeanFactory());
		}
		catch (BeansException e) {
			throw e;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new BeanCreationException("Cannot register from " + getClass(), e);
		}
	}

	protected void register(BeanDefinitionRegistry registry,
			ConfigurableListableBeanFactory factory) throws Exception {
		AutoRegistrar registrar = new AutoRegistrar(registry, factory, getEnvironment(),
				getResourceLoader());
		for (Class<?> type : config()) {
			registrar.register(type);
		}
	}

}