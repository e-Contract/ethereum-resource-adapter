/*
 * Ethereum JCA Resource Adapter Project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.rar.demo;

import be.e_contract.ethereum.rar.demo.model.EthereumBean;
import be.e_contract.ethereum.rar.demo.model.RollbackException;
import be.e_contract.ethereum.ra.EthereumConnection;
import be.e_contract.ethereum.ra.EthereumConnectionFactory;
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
public class EthereumServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthereumServlet.class);

    @Resource(mappedName = "java:/EthereumConnectionFactory")
    private EthereumConnectionFactory ethereumConnectionFactory;

    @EJB
    private EthereumBean ethereumBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter output = resp.getWriter();
        try {
            try (EthereumConnection ethereumConnection = (EthereumConnection) this.ethereumConnectionFactory.getConnection()) {
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
