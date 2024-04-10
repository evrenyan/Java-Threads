import java.util.List; import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock; import java.util.concurrent.locks.ReentrantLock;
class Bord {
    private List<String> kopper = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    private Condition ikkeTomt = lock.newCondition();
    private int totaltAntallKopper;
    public Bord(int totaltAntallKopper) {
        this.totaltAntallKopper = totaltAntallKopper;
    }
    public void serverKaffe(String kaffe) {
        lock.lock();
        try {
            kopper.add(kaffe);
            totaltAntallKopper--;
            ikkeTomt.signalAll();
        } finally {
            lock.unlock();
        }
    }
    public String hentKaffe() throws InterruptedException {
        lock.unlock();
        try {
            while (kopper.isEmpty() && totaltAntallKopper > 0) {
                ikkeTomt.await();
            }
            if (!kopper.isEmpty()) {
                return kopper.remove(0);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
}
class Barista implements Runnable {
    private Bord bord;
    private int id;
    private int antKaffe;
    private final String[] drikker = {"Americano", "Café au lait", "Caffè latte", "Caffè mocca", "Espresso", "Cortado"};
    public Barista(Bord bord, int id, int antKaffe) {
        this.antKaffe = antKaffe;
        this.bord = bord;
        this.id = id;
    }
    @Override
    public void run() {
        Random rand = new Random();
        for (int i = 0; i < antKaffe; i++) {
            int indeks = rand.nextInt(drikker.length);
            String drikke = drikker[indeks];
            bord.serverKaffe(drikke);
            System.out.println("Barista " + id + " serverer en " + drikke);
        }
        System.out.println("Barista " + id + " er ferdig med å servere kaffe.");
    }
}
class Kaffedrikker implements Runnable {
    private Bord bord;
    private int id;
    public Kaffedrikker(Bord bord, int id) {
        this.bord = bord;
        this.id = id;
    }
    @Override
    public void run() {
        int antallDrukket = 0;
        try {
            String kaffe;
            while((kaffe = bord.hentKaffe()) != null) {
                System.out.println("Kaffedrikker " + id + " drikker en " + kaffe);
                antallDrukket++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Kaffedrikker " + id + " ble avbrutt.");
        } finally {
            System.out.println("Kaffedrikker " + id + " har drukket " + antallDrukket + " kopp(er) kaffe."); }
        }
    }
