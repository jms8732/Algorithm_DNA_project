import sun.rmi.runtime.Log;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class reconstruct {
    private static final int ERROR = 2; //error threshold
    private static final double SEC = 1000.0;
    private Vector<Vector<Object>> textTable;
    private Vector<Vector<Object>> patternTable;
    private String[] pair = {"AA", "AC", "AT", "AG", "CA", "CC", "CT", "CG", "TA", "TC", "TT", "TG", "GA", "GC", "GT", "GG"};
    private HashMap<String, Integer> map;
    private long start, end, result;
    private int N;

    reconstruct(int n) {
        this.N = n;
        init();
        textMakeTable();
    }

    //EMPM 테이블 생성하는 메소드
    private void init() {
        map = new HashMap<>();
        for (int i = 0; i < pair.length; i++) {
            int tmp = 0;
            //Little Endian
            for (int j = pair[i].length() - 1; j >= 0; j--) {
                char c = pair[i].charAt(j);
                if (j == 1) {
                    tmp = c;
                    tmp = tmp << 8;
                    System.out.print(Double.toHexString((double) tmp));
                } else {
                    tmp |= c;
                }
            }
            int p1 = (tmp & 1536) >> 9;
            int p2 = (tmp & 6) << 1;
            int hash = p1 + p2;
            map.put(pair[i], hash);
        }
    }

    //text에 대한 테이블 생성
    private void textMakeTable() {
        textTable = new Vector<>();
        File file;
        FileReader fr;
        BufferedReader br;
        //text테이블에 pair 추가
        for (int i = 0; i < pair.length; i++) {
            Vector<Object> v = new Vector<>();
            v.add(pair[i]); //pair
            v.add(0); //count
            textTable.add(v);
        }
        try {
            file = new File("myDNA.txt");
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String tmp = br.readLine();
            int cur = -1; //현재 인덱스
            int idx = 0; //벡터의 인덱스
            for (int i = 0; i < tmp.length(); i++) {
                if (cur + 2 <= tmp.length() - 1) {
                    String s1 = tmp.substring(++cur, cur + 2);
                    idx = map.get(s1);
                    Vector<Object> v = textTable.get(idx);
                    count(v);
                    tableInsertIndex(v, cur);
                } else
                    break;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //pattern에 대한 테이블 생성, 입력값으로 패턴을 입력받는다.
    private void patternMakeTable(String s) {
        patternTable = new Vector<>();
        //pattern 테이블에 pair 추가
        for (int i = 0; i < pair.length; i++) {
            Vector<Object> v = new Vector<>();
            v.add(pair[i]); //pair
            v.add(false); //패턴을 지나갔는지에 대한 여부를 판단하기 위한 불리언 값
            v.add(0); //count
            patternTable.add(v);
        }
        int cur = -1; //현재 인덱스
        int idx = 0; //벡터의 인덱스
        for (int i = 0; i < s.length(); i++) {
            if (cur + 1 <= s.length() - 1) {
                String s1 = s.substring(++cur, cur + 2);
                idx = map.get(s1);
                Vector<Object> v = patternTable.get(idx);
                patternCount(v);
                tableInsertIndex(v, cur);
                ++cur;

            } else
                break;
        }
        for (Vector<Object> c : patternTable) {
            if ((int) c.get(2) == 0) {
                c.remove(1);
                c.add(1, true);
            }
        }
    }

    //사용자에게 글자를 입력받으면 해당 글자에 맞는 알고리즘을 수행하는 메소드
    public void start() throws IOException {
        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("BruteForce : b , EMPM Algorithm : e , 종료 : q ");
        Scanner scanner = new Scanner(System.in);
        String c = scanner.next();
        char s = c.charAt(0);
        while (s != 'q') {
            File file = new File("myShortRead.txt");
            File file1 = new File("humanDNA.txt");
            FileReader fr = new FileReader(file);
            FileReader fr1 = new FileReader(file1);
            BufferedReader br = new BufferedReader(fr);
            BufferedReader br1 = new BufferedReader(fr1);
            String tmp, tmp1;
            switch (s) {
                case 'b':
                    tmp = br1.readLine();
                    start = System.currentTimeMillis();
                    while ((tmp1 = br.readLine()) != null) {
                        bruteForce(tmp1, tmp);
                    }
                    errorCheck1();
                    end = System.currentTimeMillis();
                    result = (end - start);
                    System.out.println("brute Algorithm time :  " + result / SEC + "sec , " + (result /SEC) / 60 +"min");
                    break;
                case 'e':
                    tmp = br1.readLine(); //text
                    start = System.currentTimeMillis();
                    while ((tmp1 = br.readLine()) != null) {
                        patternMakeTable(tmp1);
                        // EMPMAlgorithm(tmp1, tmp);
                        algorithm(tmp1, tmp);
                    }
                    end = System.currentTimeMillis();
                    errorCheck();
                    result = (end-start);
                    System.out.println("EMPM Algorithm time : " + result / SEC + "sec, " + (result/SEC)/60 + "min");
                    break;
                case 'q':
                    System.out.println("시스템을 종료합니다");
                    break;
            }
            fr.close();
            fr1.close();
            br.close();
            br1.close();
            System.out.println("----------------------------------------------");
            System.out.println("BruteForce : b , EMPMAlgorithm : e , 종료 : q ");
            c = scanner.next();
            s = c.charAt(0);
        }
        System.out.println("시스템을 종료합니다.");
    }

    //bruteforce를 수행하는 메소드
    private void bruteForce(String patternString, String textString) throws IOException {
        boolean check = false;
        int errorCount = 0;
        int i, j;
        for (i = 0; i < textString.length() - patternString.length(); i++) {
            //text와 pattern을 계속 비교
            for (j = 1; j < patternString.length(); j++) {
                if (textString.charAt(i + j) != patternString.charAt(j))
                    errorCount++;
            }
            if (errorCount <= ERROR) {
                File file = new File("test1.txt");
                BufferedReader br = new BufferedReader(new FileReader(file));
                String tmp2 = br.readLine(); //파일 읽어옴
                StringBuilder sb = new StringBuilder(tmp2);
                sb.replace(i, i + patternString.length(), patternString);
                tmp2 = sb.toString();
                br.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(tmp2); //패턴 작성
                bw.flush();
                bw.close();
                break;
            }
            errorCount = 0;
        }
    }

    //EMPM알고리즘을 수행하는 메소드
    private void algorithm(String patternString, String textString) {
        Vector<Object> pv = null;
        Vector<Object> tv = null;
        //patternTable에서 가장 적게 등장한 문자열 쌍과 인덱스 파악
        int idx = minCounter(pv);
        pv = patternTable.get(idx);
        tv = textTable.get(idx);

        int textidx = 2;
        int patternidx = 3;
        int patternStart = (int) pv.get(patternidx);
        statusCheck(pv); //방문하는 패턴의 값을 true값으로 바꿈
        int textStart = (int) tv.get(textidx);
        int first = textStart - patternStart; //text의 현재 존재하는 좌표
        boolean check = false;
        boolean check1 = false;
        int errorCount = 0;
        String tmp = null;
        String tmp1 = null;

        while (!check) {
            //텍스트의 위치가 0이상이여야한다
            patternStart = 0;
            if (first >= 0) {
                for (int i = first; i < first + patternString.length(); i += 2) {
                    //텍스트 스트링 범위보다 높을 경우 탈출
                    if (i + 2 > textString.length()) {
                        check1 = true;
                        break;
                    }
                    tmp = textString.substring(i, i + 2);
                    tmp1 = patternString.substring(patternStart, patternStart + 2);
                    int hashT = map.get(tmp);
                    int hashS = map.get(tmp1);
                    if (hashT != hashS) {
                        for (int j = 0; j < tmp.length(); j++) {
                            if (tmp.charAt(j) != tmp1.charAt(j))
                                errorCount++; //에러 증가
                        }
                    }
                    //에러가 threshold보다 높으면 반복문 탈출
                    if (errorCount > ERROR)
                        break;
                    patternStart += 2; //패턴만 바꾼다.
                }
                //텍스트 범위내 있을 경우
                if (!check1) {
                    //에러도 threshold보다 적을 경우 파일 작성
                    if (errorCount <= ERROR) {
                        check = true;
                        try {
                            File file = new File("test.txt");
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String tmp2 = br.readLine(); //파일 읽어옴
                            StringBuilder sb = new StringBuilder(tmp2);
                            sb.replace(first, first + patternString.length(), patternString);
                            tmp2 = sb.toString();
                            br.close();
                            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                            bw.write(tmp2); //패턴 작성
                            bw.flush();
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        errorCount = 0;
                    } else {
                        //패턴 idx를 증가시킨다.
                        patternidx++;
                        if (patternidx < pv.size()) {
                            textStart = (int) tv.get(textidx);
                            patternStart = (int) pv.get(patternidx);
                            first = textStart - patternStart;
                        } else {
                            //패턴 idx를 증가시켯는데도 없는 경우
                            textidx++;
                            patternidx = 3;
                            if (textidx < tv.size()) {
                                textStart = (int) tv.get(textidx);
                                patternStart = (int) pv.get(patternidx);
                                first = textStart - patternStart;
                            } else {
                                //텍스트를 증가시켰는데도 없는 경우, 패턴을 바꾼다.
                                String tmp2 = (String) pv.get(0);
                                if (!escape(patternTable)) { //에러 탈출을 위해서 패턴 테이블에서 모든 패턴이 다 true가 아닌 경우 수행
                                    idx = minCounter(pv, tmp2);
                                    pv = patternTable.get(idx);
                                    tv = textTable.get(idx);
                                    textidx = 2;
                                    patternidx = 3;
                                    statusCheck(pv);
                                    patternStart = (int) pv.get(patternidx);
                                    textStart = (int) tv.get(textidx);
                                    first = textStart - patternStart;
                                } else
                                    check = true; //패턴을 버린다.
                            }
                        }
                    }
                } else { //텍스트 범위내 없는 경우, 패턴을 돌린다.
                    patternidx++;
                    if (patternidx < pv.size()) {
                        textStart = (int) tv.get(textidx);
                        patternStart = (int) pv.get(patternidx);
                        first = textStart - patternStart;
                    } else {
                        //패턴 idx를 증가시켯는데도 없는 경우
                        textidx++;
                        patternidx = 3;
                        if (textidx < tv.size()) {
                            textStart = (int) tv.get(textidx);
                            patternStart = (int) pv.get(patternidx);
                            first = textStart - patternStart;
                        } else {
                            //텍스트를 증가시켰는데도 없는 경우, 패턴을 바꾼다.
                            String tmp2 = (String) pv.get(0);
                            if (!escape(patternTable)) { //에러 탈출을 위해서 패턴 테이블에서 모든 패턴이 다 true가 아닌 경우 수행
                                idx = minCounter(pv, tmp2);
                                pv = patternTable.get(idx);
                                tv = textTable.get(idx);
                                textidx = 2;
                                patternidx = 3;
                                statusCheck(pv);
                                patternStart = (int) pv.get(patternidx);
                                textStart = (int) tv.get(textidx);
                                first = textStart - patternStart;
                            } else
                                check = true; //패턴을 버린다.
                        }
                    }
                }
                errorCount = 0;
            } else {
                //텍스트의 인덱스 위치가 0일 경우
                patternidx++;
                if (patternidx < pv.size()) {
                    textStart = (int) tv.get(textidx);
                    patternStart = (int) pv.get(patternidx);
                    first = textStart - patternStart;
                } else {
                    //패턴 idx를 증가시켯는데도 없는 경우
                    textidx++;
                    patternidx = 3;
                    if (textidx < tv.size()) {
                        textStart = (int) tv.get(textidx);
                        patternStart = (int) pv.get(patternidx);
                        first = textStart - patternStart;
                    } else {
                        //텍스트를 증가시켰는데도 없는 경우, 패턴을 바꾼다.
                        String tmp2 = (String) pv.get(0);
                        if (!escape(patternTable)) { //에러 탈출을 위해서 패턴 테이블에서 모든 패턴이 다 true가 아닌 경우 수행
                            idx = minCounter(pv, tmp2);
                            pv = patternTable.get(idx);
                            tv = textTable.get(idx);
                            textidx = 2;
                            patternidx = 3;
                            statusCheck(pv);
                            patternStart = (int) pv.get(patternidx);
                            textStart = (int) tv.get(textidx);
                            first = textStart - patternStart;
                        } else
                            check = true; //패턴을 버린다.
                    }
                }
                errorCount = 0;
            }

        }
    }

    //벡터의 인덱스 1에 저장되어있는 boolean의 값을 채크하는 메소드
    private void statusCheck(Vector<Object> v) {
        boolean check = (boolean) v.get(1);
        if (!check)
            check = true;
        v.remove(1);
        v.add(1, check);
    }

    //벡터의 인덱스 1에 저장되있는 boolean 값이 모두 true일 경우, false 반환한다.
    private boolean escape(Vector<Vector<Object>> v) {
        boolean check = false;
        for (Vector<Object> c : v) {
            if ((boolean) c.get(1) == true)
                check = true;
            else {
                check = false;
                return check;
            }
        }
        return check;
    }


    //패턴 테이블에 저장되어있는 가장 최소의 패턴을 찾기 위해 호출되는 메소드
    private int minCounter(Vector<Object> v, String t) {
        int min = 0;
        int idx = 0;
        for (int i = 0; i < patternTable.size(); i++) {
            v = patternTable.get(i);
            if (min == 0) {
                if ((int) v.get(2) != 0 && !(boolean) v.get(1)) {
                    min = (int) v.get(2);
                    idx = i;
                }
            } else {
                if ((int) v.get(2) != 0 && min > (int) v.get(2) && !(boolean) v.get(1)) {
                    min = (int) v.get(2);
                    idx = i;
                }
            }
        }
        return idx;
    }

    //패턴 테이블에 저장되어있는 가장 최소의 패턴을 찾기 위해 호출되는 메소드
    private int minCounter(Vector<Object> v) {
        int min = 0;
        int idx = 0;
        for (int i = 0; i < patternTable.size(); i++) {
            v = patternTable.get(i);
            if (min == 0) {
                if ((int) v.get(2) != 0) {
                    min = (int) v.get(2);
                    idx = i;
                }
            } else {
                if ((int) v.get(2) != 0 && min > (int) v.get(2)) {
                    min = (int) v.get(2);
                    idx = i;
                }
            }
        }
        return idx;
    }

    //패턴 테이블을 생성할 때, 해당 패턴이 몇번 등장했는지 count를 하는 메소드
    private void patternCount(Vector<Object> v) {
        //count를 추가
        int count = (int) v.get(2);
        count += 1;
        v.remove(2);
        v.add(2, count);
    }

    //count를 하는 메소드
    private void count(Vector<Object> v) {
        //count를 추가
        int count = (int) v.get(1);
        count += 1;
        v.remove(1);
        v.add(1, count);
    }

    private void tableInsertIndex(Vector<Object> v, int cur) {
        //현재 인덱스를 벡터에 추가
        v.add(cur);
    }

    //bruteforce의 에러율를 채크하기 하기 위한 메소드
    private void errorCheck() throws IOException {
        File file = new File("test.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        String tmp = br.readLine();
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) == '-')
                count++;
        }
        System.out.println("empty count : " + count);
        double errorRate = ((double)count / this.N);
        System.out.println("error rate : " + ((double) count / this.N) + "%");
        double correct = 100.0 - errorRate;
        System.out.println("Correct rate : "  + correct + "%" );
        br.close();
    }

    //EMPM알고리즘의 에러율를 확인하기 위한 메소드
    private void errorCheck1() throws IOException {
        File file = new File("test1.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        String tmp = br.readLine();
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) == '-')
                count++;
        }
        System.out.println("empty count : " + count);
        double errorRate = ((double)count / this.N);
        System.out.println("error rate : " + ((double) count / this.N) + "%");
        double correct = 100.0 - errorRate;
        System.out.println("Correct rate : "  + correct + "%" );
        br.close();
    }
}
