package SearchAlgorithm;
import java.util.Scanner;

/*
 * 보초법 (SeqSearch + Sentinel)
 * 
 * 선형 검색은 반복할 때마다 종료 조건 1, 2를 모두 판단
 * 보초법은 이 비용을 반으로 줄일 수 있다.
 * 
 * 검색하기 전, 검색하려는 키 값을 맨 끝 요소에 저장한다. 
 * 이때 저장하는 값을 보초(sentinel)라고 한다.
 * 
 * 종료조건
 * 2. (원하는 값이 데이터에 존재하지 않아도) 보초까지만 검색하면 종료
 * 	-> 조건 1이 없어도 되기 때문에 '보초는 반복문에서 종료 판단 횟수를 2회에서 1회로 줄이는 역할'을 한다.
 * */
public class SeqSearchSen {
	
	//요솟수가 n인 배열 a에서 key와 같은 요소를 보초법으로 선형 검색한다.
	static int seqSearchSen(int[] a, int n, int key) {
		int i = 0;
		
		a[n] = key;	//보초 추가: 검색할 값 key를 보초로 a[n]에 대입한다.
		
		while(true) {
//			if(i == key)		//이 부분 때문에 계속 막혔음.
			if(a[i] == key) 	//검색 성공
				break;
			i++;
		}
		return i == n ? -1 : i;	//반복이 완료되면, 찾은 값의 배열이 원래 데이터인지 보초인지 판단한다.
	}							//i == n이면 찾은 값이 보초이므로 -1 반환 (검색 실패)
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.print("요솟수 : ");
		int num = sc.nextInt();
		int[] arr = new int[num + 1];	//요솟수 num + 1
		
		for(int i = 0; i < num; i++) {
			System.out.print("arr[" + i + "] : ");
			arr[i] = sc.nextInt();
		}
		
		System.out.print("검색할 값 : ");	//키 값을 입력한다.
		int key = sc.nextInt();
		
		int idx = seqSearchSen(arr, num, key);	//배열 arr에서 키 값이 key인 요소를 검색한다.
		
		if(idx == -1)
			System.out.println(key + "은(는) 없는 요소입니다.");
		
		else
			System.out.println(key + "은(는) arr[" + idx + "]에 있습니다.");
	}
	
	
}
