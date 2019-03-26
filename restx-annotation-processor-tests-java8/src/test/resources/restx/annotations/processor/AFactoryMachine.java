package com.example;

import com.google.common.collect.ImmutableSet;
import restx.factory.*;
import com.example.A;

@Machine
public class AFactoryMachine extends SingleNameFactoryMachine<com.example.A> {
    public static final Name<com.example.A> NAME = Name.of(com.example.A.class, "A");

    public AFactoryMachine() {
        super(0, new StdMachineEngine<com.example.A>(NAME, 0, BoundlessComponentBox.FACTORY) {


            @Override
            public BillOfMaterials getBillOfMaterial() {
                return new BillOfMaterials(ImmutableSet.<Factory.Query<?>>of(

                ));
            }

            @Override
            protected com.example.A doNewComponent(SatisfiedBOM satisfiedBOM) {
                return new A(

                );
            }
        });
    }

}
