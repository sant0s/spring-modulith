/*
 * Copyright 2019-2020 the original author or authors.
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
package org.moduliths.model;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.acme.withatbean.TestEvents.JMoleculesAnnotated;
import com.acme.withatbean.TestEvents.JMoleculesImplementing;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Unit tests for {@link Module}.
 *
 * @author Oliver Drotbohm
 */
@TestInstance(Lifecycle.PER_CLASS)
class ModuleUnitTest {

	ClassFileImporter importer = new ClassFileImporter();
	JavaClasses classes = importer.importPackages("com.acme.withatbean"); //
	JavaPackage javaPackage = JavaPackage.of(Classes.of(classes), "");

	Module module = new Module(javaPackage, false);

	@Test
	public void considersExternalSpringBeans() {

		assertThat(module.getSpringBeans()) //
				.flatExtracting(SpringBean::getFullyQualifiedTypeName) //
				.contains(DataSource.class.getName());
	}

	@Test
	void discoversPublishedEvents() {

		JavaClass jMoleculesAnnotated = classes.get(JMoleculesAnnotated.class);
		JavaClass jMoleculesImplementing = classes.get(JMoleculesImplementing.class);

		List<EventType> events = module.getPublishedEvents();

		assertThat(events.stream().map(EventType::getType)) //
				.containsExactlyInAnyOrder(jMoleculesAnnotated, jMoleculesImplementing);
		assertThat(events.stream().filter(it -> it.getType().equals(jMoleculesAnnotated))) //
				.element(0) //
				.satisfies(it -> {
					assertThat(it.getSources()).isNotEmpty();
				});
	}
}
