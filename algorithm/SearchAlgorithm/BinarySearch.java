package SearchAlgorithm;
import java.util.Scanner;

/*
 * 이진 검색 (Binary Search)
 * */

public class BinarySearch {
	//요솟수가 n인 배열 a에서 key와 같은 요소를 이진 검색한다.
	static int binarySearch(int[] a, int n, int key) {
		int pl = 0;		//검색 범위의 첫 인덱스
		int pr = n - 1;	//검색 범위의 끝 인덱스
		
		do {
			int pc = (pl + pr) / 2;		// 중앙 요소의 인덱스
			
			if(a[pc] == key)
				return pc;				//검색 성공!
			
			else if(a[pc] < key)
				pl = pc + 1;			//검색 범위를 뒤쪽 절반으로 좁힘
			
			else
				pr = pc - 1;			//검색 실패!
		} while(pl <= pr);
		
		return -1;
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.print("요솟수 : ");
		int num = sc.nextInt();
		int[] arr = new int[num];
		
		System.out.println("오름차순으로 입력하세요.");
		
		System.out.print("arr[0] : ");
		arr[0] = sc.nextInt();
		
		for(int i = 1; i < num; i++) {	//첫 번째 요소 입력했기 때문에 초기값 1
			do{
				System.out.print("arr[" + i + "] : ");
				arr[i] = sc.nextInt();
			} while(arr[i] < arr[i-1]);	//바로 앞의 요소보다 작으면 다시 입력
		}
		
		System.out.print("검색할 값 : ");
		int key = sc.nextInt();
		
		int idx = binarySearch(arr, num, key);	//배열 arr에서 키 값이 key인 요소를 검색한다.
		
		if(idx == -1)
			System.out.println(key + "은(는) 없는 요소입니다.");
		
		else
			System.out.println(key + "은(는) arr[" + idx + "]에 있습니다.");
	}
}
