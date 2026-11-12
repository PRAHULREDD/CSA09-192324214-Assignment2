import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PlaylistBenchmark {

    static class Song {
        final int id;
        final String title;
        final String artist;

        Song(int id, String title, String artist) {
            this.id = id;
            this.title = title;
            this.artist = artist;
        }
    }

    private static List<Song> loadSeedSongs(String filename) {
        List<Song> seeds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    seeds.add(new Song(id++, parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException ignored) {
        }
        return seeds;
    }

    private static void benchmarkSize(int size, int opsCount) {
        System.out.println("=========================================================================");
        System.out.printf("PLAYLIST SIZE: %,d songs | Operations per test: %,d%n", size, opsCount);
        System.out.println("=========================================================================");

        Song sample = new Song(999999, "Naatu Naatu", "Rahul Sipligunj");

        // 1. Add at end
        long t0 = System.nanoTime();
        List<Song> alEnd = new ArrayList<>();
        for (int i = 0; i < size; i++) alEnd.add(new Song(i, "Track " + i, "Artist"));
        long alAddEndNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        List<Song> llEnd = new LinkedList<>();
        for (int i = 0; i < size; i++) llEnd.add(new Song(i, "Track " + i, "Artist"));
        long llAddEndNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        ArrayDeque<Song> adEnd = new ArrayDeque<>();
        for (int i = 0; i < size; i++) adEnd.addLast(new Song(i, "Track " + i, "Artist"));
        long adAddEndNs = System.nanoTime() - t0;

        // 2. Insert & Remove at Front (index 0)
        List<Song> alFront = new ArrayList<>(alEnd);
        t0 = System.nanoTime();
        for (int i = 0; i < opsCount; i++) {
            alFront.add(0, sample);
            alFront.remove(0);
        }
        long alFrontNs = System.nanoTime() - t0;

        LinkedList<Song> llFront = new LinkedList<>(llEnd);
        t0 = System.nanoTime();
        for (int i = 0; i < opsCount; i++) {
            llFront.addFirst(sample);
            llFront.removeFirst();
        }
        long llFrontNs = System.nanoTime() - t0;

        ArrayDeque<Song> adFront = new ArrayDeque<>(adEnd);
        t0 = System.nanoTime();
        for (int i = 0; i < opsCount; i++) {
            adFront.addFirst(sample);
            adFront.removeFirst();
        }
        long adFrontNs = System.nanoTime() - t0;

        // 3. Insert & Remove in Middle (size / 2)
        List<Song> alMid = new ArrayList<>(alEnd);
        int mid = size / 2;
        t0 = System.nanoTime();
        for (int i = 0; i < opsCount; i++) {
            alMid.add(mid, sample);
            alMid.remove(mid);
        }
        long alMidNs = System.nanoTime() - t0;

        LinkedList<Song> llMid = new LinkedList<>(llEnd);
        t0 = System.nanoTime();
        for (int i = 0; i < opsCount; i++) {
            llMid.add(mid, sample);
            llMid.remove(mid);
        }
        long llMidNs = System.nanoTime() - t0;

        // 4. Random Access: get(k)
        Random rng = new Random(42);
        int[] indices = new int[opsCount];
        for (int i = 0; i < opsCount; i++) indices[i] = rng.nextInt(size);

        long sink = 0;
        t0 = System.nanoTime();
        for (int idx : indices) sink += alEnd.get(idx).id;
        long alGetNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int idx : indices) sink += llEnd.get(idx).id;
        long llGetNs = System.nanoTime() - t0;

        // 5. Full Iteration
        t0 = System.nanoTime();
        for (Song s : alEnd) sink += s.id;
        long alIterNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (Song s : llEnd) sink += s.id;
        long llIterNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (Song s : adEnd) sink += s.id;
        long adIterNs = System.nanoTime() - t0;

        if (sink == -1) System.out.print("");

        System.out.printf("%-26s | %12s | %12s | %12s%n", "Operation", "ArrayList", "LinkedList", "ArrayDeque");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-26s | %9.3f ms | %9.3f ms | %9.3f ms%n", "Build / Add at End",
                alAddEndNs / 1e6, llAddEndNs / 1e6, adAddEndNs / 1e6);
        System.out.printf("%-26s | %9.3f ms | %9.3f ms | %9.3f ms%n", "Insert + Remove at Front",
                alFrontNs / 1e6, llFrontNs / 1e6, adFrontNs / 1e6);
        System.out.printf("%-26s | %9.3f ms | %9.3f ms | %12s%n", "Insert + Remove at Middle",
                alMidNs / 1e6, llMidNs / 1e6, "N/A (No idx)");
        System.out.printf("%-26s | %9.3f ms | %9.3f ms | %12s%n", "Random Access get(k)",
                alGetNs / 1e6, llGetNs / 1e6, "N/A (No idx)");
        System.out.printf("%-26s | %9.3f ms | %9.3f ms | %9.3f ms%n%n", "Full Sequential Iteration",
                alIterNs / 1e6, llIterNs / 1e6, adIterNs / 1e6);
    }

    public static void main(String[] args) {
        List<Song> seeds = loadSeedSongs("playlist_input.txt");
        System.out.println("Loaded " + seeds.size() + " seed songs from playlist_input.txt\n");

        int[] sizes = {1_000, 10_000, 50_000};
        for (int size : sizes) {
            benchmarkSize(size, 2_000);
        }
    }
}
