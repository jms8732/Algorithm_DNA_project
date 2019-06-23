import java.io.*;
import java.nio.Buffer;
import java.util.Random;
import java.util.Vector;

//dna를 만드는 클래스
public class generateDNA {
    private File file;
    private BufferedWriter bw;
    private char dna[] = {'A', 'C', 'G', 'T'};
    private int N, L, M;
    private Random rand;

    //생성자를 호출하게 되면 humanDNA, myDNA, shortRead를 인자인 n,l,m에 맞춰서 생성 하게 된다
    generateDNA(int n, int l,int m) {
        try {
            rand = new Random();
            this.N = n;
            this.L = l;
            this.M = m;
            humanDNA();
            myDNA();
            shortRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //human dna 생성하는 메소드
    private void humanDNA() throws IOException {
        file = new File("humanDNA.txt");
        bw = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < this.N; i++) {
            int r = rand.nextInt(3);
            bw.write(dna[r]);
        }
        bw.flush();
        bw.close();
        test();
        test1();
        System.out.println("humanDNA generate!!");
    }

    //short read를 맞는 부분에 채워 넣기 위해서 '-'로만 구성되어있는 텍스트 파일을 n길이 만큼 생성한다.
    //EMPM 알고리즘에 사용할 test
    private void test() throws IOException{
        file = new File("test.txt");
        bw = new BufferedWriter(new FileWriter(file));
        for(int i =0 ;  i< this.N; i++){
            bw.write('-');
        }
        bw.flush();
        bw.close();
    }
    //brute force에 사용할 test
    private void test1() throws IOException{
        file = new File("test1.txt");
        bw = new BufferedWriter(new FileWriter(file));
        for(int i =0 ;  i< this.N; i++){
            bw.write('-');
        }
        bw.flush();
        bw.close();
    }

    //human dna에서 1% 변형된 myDNA
    private void myDNA() throws IOException {
        double count = this.N * 0.01; //수정해야됨
        //파일을 char 형으로 가져와서 벡터에 저장
        FileReader fr = new FileReader("humanDNA.txt");
        BufferedReader br = new BufferedReader(fr);
        Vector<Character> v = new Vector<Character>();
        String tmp;
        while ((tmp = br.readLine()) != null) {
            for (int i = 0; i < tmp.length(); i++) {
                v.add(tmp.charAt(i));
            }
        }
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(this.N); //텍스트에서의 위치
            char t = v.get(r);
            int rr = rand.nextInt(3); //dna에서 랜덤한 값
            char c = dna[rr];
            while (c == t) {
                rr = rand.nextInt(3);
                c = dna[rr];
            }
            v.remove(r);
            v.add(r, c);
        }
        fr.close();
        br.close();
        //myDNA.txt 생성
        BufferedWriter bw = new BufferedWriter(new FileWriter("myDNA.txt"));
        for (char c : v)
            bw.write(c);
        bw.flush();
        bw.close();
        System.out.println("myDNA generate!!");
    }

    //myDNA의 short Read
    private void shortRead() throws IOException {
        FileReader fr = new FileReader("myDNA.txt");
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter("myShortRead.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        String tmp = null;
        tmp = br.readLine();
        //임의의 위치에서부터 시작하여 길이 L인 read를 m개 생성한다.
        for (int i = 0; i < this.M; i++) {
            int r = rand.nextInt(this.N - this.L); //임의의 위치
            String sub = tmp.substring(r, r + this.L);
            bw.write(sub);
            bw.newLine();
            bw.flush();
        }
        fr.close();
        br.close();
        fw.close();
        bw.close();

        System.out.println("shortRead generate!!");

    }
}
