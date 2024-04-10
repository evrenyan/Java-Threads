import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Sykehus {
    private final Queue<String> pasientKoe = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition ikkeTom = lock.newCondition();
    private boolean allePasienterLagtTil = false;
    public void leggTilPasient(String pasientId) {
        lock.lock();
        try  {
            pasientKoe.add(pasientId);
            System.out.println("Pasient mottatt: " + pasientId);
            ikkeTom.signalAll();
        } finally {
            lock.unlock();
        }
    }
    public void behandlePasient() {
        lock.lock();
        try {
            while (!(allePasienterLagtTil &&pasientKoe.isEmpty())) {
                while (pasientKoe.isEmpty()) {
                    if (!allePasienterLagtTil) {
                        ikkeTom.await();
                    } else {
                        return;
                    }
                }
                String pasientId = pasientKoe.poll();
                System.out.println("behandler pasient: " + pasientId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
    public void signaliserAllePasienterLagtTil() {
        lock.lock();
        try {
            allePasienterLagtTil = true;
            ikkeTom.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
class MottaPasienter extends Thread {
    private final Sykehus sykehus;
    private final int antallPasienter;
    public MottaPasienter(Sykehus sykehus, int antallPasienter) {
        this.antallPasienter = antallPasienter;
        this.sykehus = sykehus;
    }
    @Override
    public void run() {
        for (int i = 1; i <= antallPasienter; i++) {
            sykehus.leggTilPasient("pasient" +i);
        }
        sykehus.signaliserAllePasienterLagtTil();
    }
}
class BehandlePasienter extends Thread {
    private final Sykehus sykehus;
    public BehandlePasienter(Sykehus sykehus) {

    }
}
class BehandlePasienter extends Thread {
    private final Sykehus sykehus;
    public BehandlePasienter(Sykehus sykehus) {
        this.sykehus = sykehus;
    }
    @Override
    public void run() {
        sykehus.behandlePasient();
    }
}
class Main {
    public static void main(String[] args) {
        Sykehus sykehus = new Sykehus();
        MottaPasienter mottaPasienter = new MottaPasienter(sykehus, 20);
        mottaPasienter.start();

        BehandlePasienter behandlePasienter = new BehandlePasienter(sykehus);
        behandlePasienter.start();
    }
}
