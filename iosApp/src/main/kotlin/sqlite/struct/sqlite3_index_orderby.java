package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;

@Generated
@Structure()
public final class sqlite3_index_orderby extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_index_orderby() {
		super(sqlite3_index_orderby.class);
	}

	@Generated
	protected sqlite3_index_orderby(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_index_orderby(int iColumn, byte desc) {
		super(sqlite3_index_orderby.class);
		setIColumn(iColumn);
		setDesc(desc);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native int iColumn();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setIColumn(int value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native byte desc();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setDesc(byte value);
}