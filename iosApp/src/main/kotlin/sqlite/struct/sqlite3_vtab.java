package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ptr.BytePtr;
import com.intel.inde.moe.natj.general.ptr.VoidPtr;

@Generated
@Structure()
public final class sqlite3_vtab extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_vtab() {
		super(sqlite3_vtab.class);
	}

	@Generated
	protected sqlite3_vtab(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_vtab(VoidPtr pModule, int nRef, BytePtr zErrMsg) {
		super(sqlite3_vtab.class);
		setPModule(pModule);
		setNRef(nRef);
		setZErrMsg(zErrMsg);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native VoidPtr pModule();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setPModule(VoidPtr value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native int nRef();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setNRef(int value);

	@Generated
	@StructureField(order = 2, isGetter = true)
	public native BytePtr zErrMsg();

	@Generated
	@StructureField(order = 2, isGetter = false)
	public native void setZErrMsg(BytePtr value);
}