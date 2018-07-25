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

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Dave Syer
 *
 */
public class BeansInitializer
		implements ApplicationContextInitializer<GenericApplicationContext> {

	private Class<?> type;

	public BeansInitializer(Class<?> type) {
		this.type = type;
	}

	@Override
	public void initialize(GenericApplicationContext context) {
		new AutoRegistrar(context, context.getDefaultListableBeanFactory(),
				context.getEnvironment(), context).register(type);
	}

}
