                SingleComponentBoxSupplier.of(Name.of({type}.class, "{injectionName}"), BoundlessComponentBox.FACTORY,
                        new SingleComponentSupplier<{type}>() {
                            @Override
                            public {type} newComponent(Factory factory) {
                                return module.{name}(
                                    {parameters}
                                );
                            }
                        })