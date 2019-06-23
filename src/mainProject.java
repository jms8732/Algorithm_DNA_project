import java.io.IOException;
import java.util.Scanner;

public class mainProject {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.print("DNA의 길이 : " );
        int N = scanner.nextInt();
        System.out.print("Map 의 개수 : " );
        int M = scanner.nextInt();
        System.out.print("short Read 길이 : ");
        int L = scanner.nextInt();
        generateDNA gda = new generateDNA(N,L,M); //각각의 text파일을 생성한다.
        reconstruct r = new reconstruct(M); //알고리즘 비교를 수행하기 위한 객체 생성
        try {
            r.start();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
