package stackserver;

public interface DataSource {
	
	byte[] addItem(byte[] item);
	
	byte[] remove();
	
}
