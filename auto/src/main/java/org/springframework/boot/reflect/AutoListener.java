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

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.StandardEnvironment;

/**
 * Listener that sets up an {@link ApplicationContextInitializer} to register functional
 * beans. Activates when the {@link SpringApplication} has a source that is an
 * ApplicationContextInitializer.
 * 
 * @author Dave Syer
 *
 */
public class AutoListener implements SmartApplicationListener {

	private StandardEnvironment environment = new StandardEnvironment();

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationStartingEvent starting = (ApplicationStartingEvent) event;
		SpringApplication application = starting.getSpringApplication();
		boolean initialized = false;
		for (Object source : application.getAllSources()) {
			Class<?> type = null;
			if (source instanceof Class<?>) {
				type = (Class<?>) source;
			}
			if (!initialized && isFunctional(type)) {
				WebApplicationType webApplicationType = application
						.getWebApplicationType();
				if (webApplicationType == WebApplicationType.REACTIVE) {
					application.setApplicationContextClass(
							ReactiveWebServerApplicationContext.class);
				}
				else if (webApplicationType == WebApplicationType.SERVLET) {
					application.setApplicationContextClass(
							ServletWebServerApplicationContext.class);
				}
				else {
					application
							.setApplicationContextClass(GenericApplicationContext.class);
				}
				application.addInitializers(new AutoInitializer(type));
				initialized = true;
			}
			if (isInitializer(type)) {
				application.addInitializers(BeanUtils.instantiateClass(type,
						ApplicationContextInitializer.class));
			} else {
				application.addInitializers(new BeansInitializer(type));
			}
		}
	}

	private boolean isFunctional(Class<?> type) {
		if (environment.getProperty("spring.functional.enabled", Boolean.class, false)) {
			return true;
		}
		return isInitializer(type);
	}

	private boolean isInitializer(Class<?> type) {
		return ApplicationContextInitializer.class.isAssignableFrom(type) && !type
				.getName().startsWith("org.springframework.cloud.bootstrap");
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationStartingEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return SpringApplication.class.isAssignableFrom(sourceType);
	}

}
