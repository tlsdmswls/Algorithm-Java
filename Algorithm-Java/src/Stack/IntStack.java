package Stack;

//int형 스택
public class IntStack {
	private int max;	//스택 용량
	private int ptr;	//스택 포인터
	private int[] stk;	//스택 본체

	/* 실행 시 예외
	 * 스택이 비어있다.
	 * */
	public class EmptyIntStackException extends RuntimeException{
		public EmptyIntStackException() {
			
		}
	}

	/* 실행 시 예외
	 * 스택이 가득 찼다.
	 * */
	public class OverflowIntStackException extends RuntimeException{
		public OverflowIntStackException() {
			
		}
	}

	//생성자
	public IntStack(int capacity) {
		ptr = 0;
		max = capacity;
		try {
			stk = new int[max];			//스택 본체용 배열 생성
		} catch (OutOfMemoryError e) {	//생성 불가
			max = 0;
		}
	}
}
