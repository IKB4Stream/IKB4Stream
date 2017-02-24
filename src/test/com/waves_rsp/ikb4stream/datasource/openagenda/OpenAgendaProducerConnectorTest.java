package com.waves_rsp.ikb4stream.datasource.openagenda;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class OpenAgendaProducerConnectorTest {

    @Test
    public void createInstanceTest() {
        new OpenAgendaProducerConnector();
    }

    @Test(expected = NullPointerException.class)
    public void testLoadNullDataProducer() {
        OpenAgendaProducerConnector open = new com.waves_rsp.ikb4stream.datasource.openagenda.OpenAgendaProducerConnector();
        open.load(null);
    }

    @Test
    public void checkNullInstanceOfOpenData() {
        OpenAgendaProducerConnector openAgendaProducerConnector = null;
        try {
            Thread thread = new Thread(() -> {
                openAgendaProducerConnector.load(dataProducer -> {
                    //Do nothing
                });
            });
            thread.start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                //Do nothing
            } finally {
                thread.interrupt();
                Thread.currentThread().interrupt();
            }
        } catch (NullPointerException err) {
            //Do nothing
        }
    }

    @Test
    public void checkBadUrlForOpenData() {
        try {
            OpenAgendaProducerConnector openAgendaProducerConnector = new OpenAgendaProducerConnector();
            Thread thread = new Thread(() -> {
                openAgendaProducerConnector.load(dataProducer -> {
                    //Do nothing
                });
            });
            thread.start();

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                //Do nothing
            } finally {
                thread.interrupt();
                Thread.currentThread().interrupt();
            }
        } catch (IllegalArgumentException e) {
            //Do nothing
        }
    }

    @Test
    public void checkIllegalPropertiesLoaded() {
        try {
            new OpenAgendaProducerConnector();
        } catch (IllegalArgumentException e) {
            //Do nothing
        }
    }

    @Test
    public void checkValidPropertiesAndData() {
        final OpenAgendaProducerConnector op = new OpenAgendaProducerConnector();
        Thread thread = new Thread(() -> {
            op.load(dataProducer -> {
                //Do Nothing
            });
        });

        thread.start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            //Do nothing
        } finally {
            thread.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void checkOpenDataPerfWithPoolThreads() {
        Thread[] threads = new Thread[10];
        IntStream.range(0, threads.length).forEach(i -> {
            threads[i] = new Thread(() -> {
                try {
                    OpenAgendaProducerConnector openAgendaProducerConnector = new OpenAgendaProducerConnector();
                    openAgendaProducerConnector.load(dataProducer -> {
                        //Do nothing
                    });

                } catch (IllegalArgumentException err) {
                    //Do nothing
                }
            });
        });

        Arrays.stream(threads).forEach(Thread::start);

        try {
            for (Thread thread : threads) {
                thread.join();
                thread.interrupt();
            }
        } catch (InterruptedException err) {
            //Do nothing
        }
    }

    @Ignore
    @Test
    public void checkIfOpenDataIsEnabled() {
        try {
            OpenAgendaProducerConnector openAgendaProducerConnector = new OpenAgendaProducerConnector();
            Assert.assertTrue(openAgendaProducerConnector.isActive());
        } catch (IllegalArgumentException err) {
            //Do nothing
        }
    }

    @Ignore
    @Test
    public void checkIfOpenDataIsDisabled() {
        try {
            OpenAgendaProducerConnector openAgendaProducerConnector = new OpenAgendaProducerConnector();
            Assert.assertFalse(openAgendaProducerConnector.isActive());
        } catch (IllegalArgumentException err) {
            //Do nothing
        }
    }

    @Test
    public void checkIllegalArgumentExceptionFromBooleanActive() {
        try {
            OpenAgendaProducerConnector openAgendaProducerConnector = new OpenAgendaProducerConnector();
            openAgendaProducerConnector.isActive();
        } catch (IllegalArgumentException e) {
            //Do nothing
        }
    }
}
