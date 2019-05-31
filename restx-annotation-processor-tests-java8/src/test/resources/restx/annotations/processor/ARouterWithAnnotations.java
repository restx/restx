package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import restx.common.Types;
import restx.common.TypeReference;
import restx.*;
import restx.entity.*;
import restx.http.*;
import restx.endpoint.*;
import restx.exceptions.WrappedCheckedException;
import restx.factory.*;
import restx.security.*;
import restx.security.PermissionFactory;
import restx.description.*;
import restx.converters.MainStringConverter;
import static restx.common.MorePreconditions.checkPresent;

import javax.validation.Validator;
import static restx.validation.Validations.checkValid;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Arrays;


@Component(priority = 0)

public class ARouter extends RestxRouter {

    public ARouter(
                    final A resource,
                    final EntityRequestBodyReaderRegistry readerRegistry,
                    final EntityResponseWriterRegistry writerRegistry,
                    final MainStringConverter converter,
                    final PermissionFactory pf,
                    final Optional<Validator> validator,
                    final RestxSecurityManager securityManager,
                    final EndpointParameterMapperRegistry paramMapperRegistry) {
        super(
                "default", "ARouter", new RestxRoute[] {

    	new StdEntityRoute<Void, Empty>("default#A#b",
                readerRegistry.<Void>build(Void.class, Optional.<String>absent()),
                writerRegistry.<Empty>build(void.class, Optional.<String>absent()),
                Endpoint.of("GET", "/abc"),
                HttpStatus.OK, RestxLogLevel.DEFAULT, pf,
                paramMapperRegistry, new ParamDef[]{
                    ParamDef.of(int.class, "num")
                }) {
            @Override
            protected Optional<Empty> doRoute(RestxRequest request, RestxResponse response, RestxRequestMatch match, Void body) throws IOException {
                securityManager.check(request, match, isAuthenticated());
                try {
                    resource.b(
                        /* [QUERY] num */ checkValid(validator, checkNotNull(mapQueryObjectFromRequest(int.class, "num", request, match, EndpointParameterKind.QUERY), "QUERY param <num> is required"),com.example.A.class)
                    );
                    return Optional.of(Empty.EMPTY);
                } catch(RuntimeException e) { throw e; }
                  catch(Exception e) { throw new WrappedCheckedException(e); }
            }

            @Override
            protected void describeOperation(OperationDescription operation) {
                super.describeOperation(operation);
                OperationParameterDescription num = new OperationParameterDescription();
                num.name = "num";
                num.paramType = OperationParameterDescription.ParamType.query;
                num.dataType = "int";
                num.schemaKey = "int";
                num.required = true;
                operation.parameters.add(num);


                operation.responseClass = "void";
                operation.inEntitySchemaKey = "";
                operation.inEntityType = Void.class;
                operation.outEntitySchemaKey = "";
                operation.outEntityType = void.class;
                operation.sourceLocation = "com.example.A#b(int)";
                operation.annotations = ImmutableList.<java.lang.annotation.Annotation>builder()

                    .add(new restx.annotations.GET() {
                        public Class<restx.annotations.GET> annotationType() { return restx.annotations.GET.class; }

                        public java.lang.String value() { return "/abc"; }

                    })

                    .add(new restx.annotations.SuccessStatus() {
                        public Class<restx.annotations.SuccessStatus> annotationType() { return restx.annotations.SuccessStatus.class; }

                        public restx.http.HttpStatus value() { return restx.http.HttpStatus.OK; }

                    })
                    
                    .add(new com.example.A.MyAnnotation() {
                        public Class<com.example.A.MyAnnotation> annotationType() { return com.example.A.MyAnnotation.class; }

                        public boolean[] severalBools() { return new boolean[]{ false, true }; }

                        public java.lang.Class<? extends java.lang.Number>[] severalParameterizedTypeClasses() { return new java.lang.Class[]{ java.lang.Integer.class, java.lang.Long.class, java.lang.Double.class }; }

                    })


                    .build();
            }
        },

        });
    }

}
