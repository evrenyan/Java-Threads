import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

class BrusAutomat {
    private static final int kapasitet = 10;
    private int brus = kapasitet;
    private final Lock lock = new ReentrantLock();
    private final Condition tom = lock.newCondition();
    private final Condition ikkeTom = lock.newCondition();

    public void refill() {
        lock.lock();
        try {
            while (brus > 0) {
                tom.await();
            }
            brus = kapasitet;
            System.out.println("brusatomaten er fylt opp!");
            ikkeTom.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    public void kjopBrus() {
        lock.lock();
        try {
            while (brus <= 0) {
                ikkeTom.await();
            }
            brus--;
            System.out.println("Du har kjøpt en brus. Gjenværende brus: " + brus);
            if (brus == 0) {
                tom.signalAll();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
class Maskinfyller extends Thread {
    private BrusAutomat brusAutomat;
    public Maskinfyller(BrusAutomat brusAutomat) {
        this.brusAutomat = brusAutomat;
    }
    @Override
    public void run() {
        try {
            while (true) {
                brusAutomat.refill();
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
class Brusdrikker extends Thread {
    private final Random random = new Random();
    private final int maskAntall;
    private int drukket = 0;
    private BrusAutomat brusAutomat;
    public Brusdrikker(BrusAutomat brusAutomat, int maksAntall) {
        this.brusAutomat = brusAutomat;
        this.maskAntall = maksAntall;
    }
    @Override
    public void run() {
        try {
            while (drukket < maskAntall) {
                Thread.sleep(random.nextInt(3000));
                brusAutomat.kjopBrus();
                drukket++;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}