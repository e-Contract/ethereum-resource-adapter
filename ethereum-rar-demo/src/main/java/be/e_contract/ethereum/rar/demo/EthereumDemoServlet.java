/*
 * Ethereum JCA Resource Adapter Project.
 * Copyright (C) 2018-2023 e-Contract.be BV.
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
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.RollbackException;
import be.e_contract.ethereum.ra.api.EthereumConnection;
import be.e_contract.ethereum.ra.api.EthereumConnectionFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.resource.ResourceException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = "/test")
public class EthereumDemoServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumDemoServlet.class);

    @Resource(lookup = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @EJB
    private EthereumBean ethereumBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter output = resp.getWriter();
        try {
            try (EthereumConnection ethereumConnection = this.ethereumConnectionFactory.getConnection()) {
                BigInteger gasPrice = ethereumConnection.getGasPrice();
                output.println("Gas Price: " + gasPrice);
            }
        } catch (ResourceException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new IOException("ethereum connection error: " + ex.getMessage(), ex);
        }

        BigInteger gasPrice;
        try {
            gasPrice = this.ethereumBean.getGasPrice(null, false);
        } catch (RollbackException ex) {
            LOGGER.error("rollback exception: " + ex.getMessage(), ex);
            return;
        }
        output.println("gas price via EJB bean: " + gasPrice);
    }
}
