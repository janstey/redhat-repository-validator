package com.redhat.repository.validator.impl.bom;

import static com.redhat.repository.validator.impl.TestUtil.dependency;
import static com.redhat.repository.validator.impl.TestUtil.pom;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Dependency;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.redhat.repository.validator.impl.AbstractTest;
import com.redhat.repository.validator.impl.bom.BomVersionPropertyException;

@ContextConfiguration
public class TestBomVersionPropertyValidator extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public IOFileFilter bomVersionPropertyValidatorFilter() {
            return FileFilterUtils.trueFileFilter();
        }

    }

    @Test
    public void shouldSuccess() {
        Dependency fooApi = dependency().artifactId("foo-api").version("${foo.version}").build();
        Dependency fooImpl = dependency().artifactId("foo-impl").version("${foo.version}").build();

        pom().artifactId("foo-bom").
                packaging("pom").
                dependencyManagement(fooApi).
                dependencyManagement(fooImpl).
                create(repoFooDir);

        validationExecutor.execute(ctx);

        assertSuccess();
    }

    @Test
    public void shouldFindDependenciesWithoutVersionProperty() {
        Dependency fooApi = dependency().artifactId("foo-api").version("1.2").build();
        Dependency fooImpl = dependency().artifactId("foo-impl").version("1.2.3").build();

        pom().artifactId("foo-bom").
                packaging("pom").
                dependencyManagement(fooApi).
                dependencyManagement(fooImpl).
                create(repoFooDir);

        validationExecutor.execute(ctx);

        assertExpectedException(BomVersionPropertyException.class, "BOM com.acme:foo-bom:1.0 contains dependencies without version property: com.acme:foo-api:1.2, com.acme:foo-impl:1.2.3");
    }

}