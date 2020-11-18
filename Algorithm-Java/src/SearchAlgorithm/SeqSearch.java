package SearchAlgorithm;
import java.util.Scanner;
/*
 * 선형 검색(Liner Search) : 배열에서 순서대로 검색하는 유일한 방법
 * 순차검색(Sequential Search)
 * 
 * 원하는 키 값을 갖는 요소를 만날 때까지 맨 앞부터 순서대로 요소를 검색한다.
 * 
 * 종료 조건
 * 1. 검색할 값을 발견하지 못하고 배열의 끝을 지나간 경우 	(검색 실패)
 * 2. 검색할 값과 같은 요소를 발견한 경우 (검색 성공)
 * 
 * 배열 요솟수가 n개일 때, 종료 조건 1, 2를 판단하는 횟수 = n/2회
 * */


/*
 * 배열 a의 처음부터 끝까지 n개의 요소를 대상으로 값이 key인 요소를 선형 검색한다.
 * 검색한 요소의 인덱스를 반환한다.
 * 값이 key인 요소가 여러 개 존재할 경우, 반환값은 처음 발견한 요소의 인덱스가 된다. (인덱스를 반환한다.)
 * 존재하지 않을 경우, -1을 반환한다.
 * */
public class SeqSearch {
	//for문이 while문보다 간단하다.
	static int seqSearch(int[] a, int n, int key) {
		for(int j = 0; j < n; j++) {
			if(a[j] == key)
				return j;
		}
		return -1;
	}
	
	static int seqSearch_while(int[] a, int n, int key) {
		int i = 0;
		
		while(true) {
			if(i == n)
				return -1;	//검색 실패, -1 반환
			
			if(a[i] == key)
				return i;	//검색 성공, 인덱스 반환
			
			i++;
		}
	}
	
	
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.print("요솟수 : ");
		int num = sc.nextInt();
		int[] arr = new int[num];
		
		for(int i = 0; i < num; i++) {
			System.out.print("arr[" + i + "] : ");
			arr[i] = sc.nextInt();
		}
		
		System.out.print("검색할 값 : ");
		int key = sc.nextInt();
		
		int idx = seqSearch(arr, num, key);	//배열 arr에서 키 값이 key인 요소를 검색한다.
		
		if(idx == -1)
			System.out.println(key + "은(는) 없는 요소입니다.");
		
		else
			System.out.println(key + "은(는) arr[" + idx + "]에 있습니다.");
	}
	
	
}
