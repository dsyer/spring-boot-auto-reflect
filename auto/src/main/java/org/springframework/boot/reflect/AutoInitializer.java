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

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 *
 */
public class AutoInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	private Class<?> type;

	public AutoInitializer(Class<?> type) {
		this.type = type;
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		AutoConfigurationPackages.register(context, ClassUtils.getPackageName(this.type));
		DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
		if (beanFactory != null) {
			if (!(beanFactory
					.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
				beanFactory
						.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
			}
			if (!(beanFactory
					.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
				beanFactory.setAutowireCandidateResolver(
						new ContextAnnotationAutowireCandidateResolver());
			}
			beanFactory.addBeanPostProcessor(
					beanFactory.createBean(AutowiredAnnotationBeanPostProcessor.class));
		}
		context.registerBean(ConfigurationPropertiesBindingPostProcessor.class);
		context.registerBean(ConfigurationBeanFactoryMetadata.BEAN_NAME,
				ConfigurationBeanFactoryMetadata.class);
		context.addBeanFactoryPostProcessor(new AutoConfigurations(context));
	}

}
