/*
 * Copyright 2012-2018 the original author or authors.
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

package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.PetController;
import org.springframework.samples.petclinic.owner.VisitController;
import org.springframework.samples.petclinic.system.CrashController;
import org.springframework.samples.petclinic.system.WelcomeController;
import org.springframework.samples.petclinic.vet.VetController;

/**
 * PetClinic Spring Boot Application.
 *
 * @author Dave Syer
 *
 */
@SpringBootApplication
public class PetClinicApplication
        implements ApplicationContextInitializer<GenericApplicationContext> {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(
                PetClinicApplication.class) {
            @Override
            protected void load(ApplicationContext context, Object[] sources) {
                // We don't want the annotation bean definition reader
                // super.load(context, sources);
            }
        };
        application.run(args);
    }

    @Override
    public void initialize(GenericApplicationContext context) {
        context.registerBean(OwnerController.class);
        context.registerBean(PetController.class);
        context.registerBean(VetController.class);
        context.registerBean(VisitController.class);
        context.registerBean(WelcomeController.class);
        context.registerBean(CrashController.class);
    }

}
