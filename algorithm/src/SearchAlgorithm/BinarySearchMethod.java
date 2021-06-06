package SearchAlgorithm;
import java.util.Arrays;
import java.util.Scanner;
/*
 * 이진 검색 메소드로 검색하기
 * 
 * 검색 과정을 자세히 출력한다.
 * */

public class BinarySearchMethod {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.print("요솟수 : ");
		int num = sc.nextInt();
		int[] arr = new int[num];
		
		System.out.println("오름차순으로 입력하세요.");
		
		System.out.print("arr[0] : ");
		arr[0] = sc.nextInt();
		
		for(int i = 1; i < num; i++) {
			do{
				System.out.print("arr[" + i + "] : ");
				arr[i] = sc.nextInt();
			} while(arr[i] < arr[i-1]);	//바로 앞의 요소보다 작으면 다시 입력함
		}
		
		System.out.print("검색할 값 : ");
		int key = sc.nextInt();
		
		int idx = Arrays.binarySearch(arr, key);	//배열 arr에서 키 값이 key인 요소를 검색함
		
//		if(idx == -1)
		if(idx < 0)
			System.out.println(key + "은(는) 없는 요소입니다.");
		
		else
			System.out.println(key + "은(는) arr[" + idx + "]에 있습니다.");

	}
}
