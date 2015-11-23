package org.neo4j.ogm.drivers.embedded.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    // a single instance of the driver's transport must be shared among all instances of the driver
    // so that we do not run into locking problems.

    private static GraphDatabaseService transport;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);


    /**
     * The default constructor will start a new embedded instance
     * using the default properties file.
     */
    public EmbeddedDriver() {
        configure(new Configuration("embedded.driver.properties"));
    }

    /**
     * This constructor allows the user to pass in an existing
     * Graph database service, e.g. if user code is running as an extension inside
     * an existing Neo4j server
     *
     * @param transport the embedded database instance
     */
    public EmbeddedDriver(GraphDatabaseService transport) {
        EmbeddedDriver.transport = transport;
        configure(new Configuration("embedded.driver.properties"));
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it
     * shuts down nicely when the VM exits (even if you "Ctrl-C" the
     * running application).
     *
     * @param graphDb the embedded instance to shutdown
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    @Override
    public synchronized void configure(Configuration config) {

        super.configure(config);

        if (transport == null) {
            String storeDir = (String) config.getConfig("neo4j.store");
            transport = new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder( storeDir )
                    .newGraphDatabase();

            registerShutdownHook(transport);
        }

        config.setConfig("transport", transport);
    }

    @Override
    public Transaction newTransaction() {   // return a new, or join an existing transaction
        return new EmbeddedTransaction(transactionManager, nativeTransaction());
    }

    @Override
    public void close() {
        if (transport != null) {
            transport.shutdown();
        }
    }

    @Override
    public Request requestHandler() {
        return new EmbeddedRequest(transport, transactionManager);
    }

    private org.neo4j.graphdb.Transaction nativeTransaction() {

        org.neo4j.graphdb.Transaction nativeTransaction;

        Transaction tx = transactionManager.getCurrentTransaction();
        if (tx != null) {
            logger.debug("Using current transaction: {}", tx);
            nativeTransaction =((EmbeddedTransaction) tx).getNativeTransaction();
        } else {
            logger.debug("No current transaction, starting a new one");
            nativeTransaction = transport.beginTx();
        }
        logger.debug("Native transaction: {}", nativeTransaction);
        return nativeTransaction;
    }
}
