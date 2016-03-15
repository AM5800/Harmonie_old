package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;

@Generated
@Structure()
public final class sqlite3_index_constraint_usage extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_index_constraint_usage() {
		super(sqlite3_index_constraint_usage.class);
	}

	@Generated
	protected sqlite3_index_constraint_usage(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_index_constraint_usage(int argvIndex, byte omit) {
		super(sqlite3_index_constraint_usage.class);
		setArgvIndex(argvIndex);
		setOmit(omit);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native int argvIndex();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setArgvIndex(int value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native byte omit();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setOmit(byte value);
}