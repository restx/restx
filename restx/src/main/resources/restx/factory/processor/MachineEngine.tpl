                new StdMachineEngine<{type}>(Name.of({type}.class, "{injectionName}"), BoundlessComponentBox.FACTORY) {
{queriesDeclarations}
                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(
{queries}
                        ));
                    }

                    @Override
                    public {type} doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return module.{name}(
{parameters}
                        );
                    }
                }
