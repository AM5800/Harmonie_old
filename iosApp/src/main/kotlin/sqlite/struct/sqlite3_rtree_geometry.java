package sqlite.struct;


import com.intel.inde.moe.natj.c.CRuntime;
import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.FunctionPtr;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ann.Runtime;
import com.intel.inde.moe.natj.general.ptr.DoublePtr;
import com.intel.inde.moe.natj.general.ptr.VoidPtr;

@Generated
@Structure()
public final class sqlite3_rtree_geometry extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_rtree_geometry() {
		super(sqlite3_rtree_geometry.class);
	}

	@Generated
	protected sqlite3_rtree_geometry(Pointer peer) {
		super(peer);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native VoidPtr pContext();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setPContext(VoidPtr value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native int nParam();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setNParam(int value);

	@Generated
	@StructureField(order = 2, isGetter = true)
	public native DoublePtr aParam();

	@Generated
	@StructureField(order = 2, isGetter = false)
	public native void setAParam(DoublePtr value);

	@Generated
	@StructureField(order = 3, isGetter = true)
	public native VoidPtr pUser();

	@Generated
	@StructureField(order = 3, isGetter = false)
	public native void setPUser(VoidPtr value);

	@Generated
	@StructureField(order = 4, isGetter = true)
	@FunctionPtr(name = "call_xDelUser")
	public native Function_xDelUser xDelUser();

	@Runtime(CRuntime.class)
	@Generated
	public interface Function_xDelUser {
		@Generated
		void call_xDelUser(VoidPtr arg0);
	}

	@Generated
	@StructureField(order = 4, isGetter = false)
	public native void setXDelUser(
			@FunctionPtr(name = "call_xDelUser") Function_xDelUser value);
}