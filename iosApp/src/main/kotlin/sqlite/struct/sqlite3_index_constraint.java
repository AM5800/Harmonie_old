package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;

@Generated
@Structure()
public final class sqlite3_index_constraint extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_index_constraint() {
		super(sqlite3_index_constraint.class);
	}

	@Generated
	protected sqlite3_index_constraint(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_index_constraint(int iColumn, byte op, byte usable,
			int iTermOffset) {
		super(sqlite3_index_constraint.class);
		setIColumn(iColumn);
		setOp(op);
		setUsable(usable);
		setITermOffset(iTermOffset);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native int iColumn();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setIColumn(int value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native byte op();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setOp(byte value);

	@Generated
	@StructureField(order = 2, isGetter = true)
	public native byte usable();

	@Generated
	@StructureField(order = 2, isGetter = false)
	public native void setUsable(byte value);

	@Generated
	@StructureField(order = 3, isGetter = true)
	public native int iTermOffset();

	@Generated
	@StructureField(order = 3, isGetter = false)
	public native void setITermOffset(int value);
}