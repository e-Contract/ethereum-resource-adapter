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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

@FacesValidator("be.e_contract.ethereum.jsf.AddressValidator")
public class AddressValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddressValidator.class);

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        if (UIInput.isEmpty(value)) {
            return;
        }
        String valueStr = (String) value;
        LOGGER.debug("validating: {}", valueStr);
        if (!WalletUtils.isValidAddress(valueStr)) {
            FacesMessage facesMessage = new FacesMessage("Invalid Ethereum address.");
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        }
        if (valueStr.toLowerCase().equals(valueStr)) {
            return;
        }
        if (!Keys.toChecksumAddress(valueStr).equals(valueStr)) {
            FacesMessage facesMessage = new FacesMessage("Ethereum address checksum error.");
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        }
    }
}
