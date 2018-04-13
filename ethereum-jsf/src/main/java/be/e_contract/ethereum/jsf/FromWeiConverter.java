/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018 e-Contract.be BVBA.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */
package be.e_contract.ethereum.jsf;

import java.math.BigDecimal;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.web3j.utils.Convert;

@FacesConverter("be.e_contract.ethereum.jsf.FromWeiConverter")
public class FromWeiConverter implements Converter {

    private Convert.Unit unit;

    public Convert.Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Convert.Unit unit) {
        this.unit = unit;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // TODO
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        BigDecimal convertedValue = Convert.fromWei(value.toString(), this.unit);
        return convertedValue.toString();
    }
}
