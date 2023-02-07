package org.xandercat.common.util;

import java.io.Serializable;

import org.xandercat.common.util.file.FileUtil;

/**
 * Class for storing a byte size along with a preferred binary unit.
 * 
 * @author Scott Arnold
 */
public class ByteSize implements Serializable {

	private static final long serialVersionUID = 2010092501L;
	
	private double value;
	private FileUtil.BinaryPrefix unit;
	private long bytes;
	
	public ByteSize(long bytes) {
		this.bytes = bytes;
		this.value = bytes;
		this.unit = FileUtil.BinaryPrefix.bytes;
		setUnit(FileUtil.getFileSizePrefix(bytes, FileUtil.BinaryPrefix.TiB));
	}
	
	public ByteSize(double value, FileUtil.BinaryPrefix unit) {
		this.unit = unit;
		this.value = value;
		this.bytes = Math.round(value * unit.getByteMultiplier());
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
		this.bytes = Math.round(value * unit.getByteMultiplier());
	}
	
	public FileUtil.BinaryPrefix getUnit() {
		return unit;
	}
	
	public void setUnit(FileUtil.BinaryPrefix unit) {
		this.value = FileUtil.convertFileSize(this.value, this.unit, unit);
		this.unit = unit;
	}
	
	public long getBytes() {
		return bytes;
	}
	
	public void setBytes(long bytes) {
		this.bytes = bytes;
		this.value = (double) bytes / (double) unit.getByteMultiplier();
	}
	
	public String toString() {
		return FileUtil.formatFileSize(bytes, unit);
	}
}
