/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.ra.oracle;

import javax.enterprise.util.AnnotationLiteral;

public class GasPriceOracleTypeQualifier extends AnnotationLiteral<GasPriceOracleType> implements GasPriceOracleType {

    private final String type;

    public GasPriceOracleTypeQualifier(String type) {
        this.type = type;
    }

    @Override
    public String value() {
        return this.type;
    }
}
