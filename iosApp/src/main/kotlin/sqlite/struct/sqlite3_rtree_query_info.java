package sqlite.struct;


import com.intel.inde.moe.natj.c.CRuntime;
import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.FunctionPtr;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ann.ReferenceInfo;
import com.intel.inde.moe.natj.general.ann.Runtime;
import com.intel.inde.moe.natj.general.ptr.DoublePtr;
import com.intel.inde.moe.natj.general.ptr.IntPtr;
import com.intel.inde.moe.natj.general.ptr.Ptr;
import com.intel.inde.moe.natj.general.ptr.VoidPtr;

@Generated
@Structure()
public final class sqlite3_rtree_query_info extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_rtree_query_info() {
		super(sqlite3_rtree_query_info.class);
	}

	@Generated
	protected sqlite3_rtree_query_info(Pointer peer) {
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

	@Generated
	@StructureField(order = 5, isGetter = true)
	public native DoublePtr aCoord();

	@Generated
	@StructureField(order = 5, isGetter = false)
	public native void setACoord(DoublePtr value);

	@Generated
	@StructureField(order = 6, isGetter = true)
	public native IntPtr anQueue();

	@Generated
	@StructureField(order = 6, isGetter = false)
	public native void setAnQueue(IntPtr value);

	@Generated
	@StructureField(order = 7, isGetter = true)
	public native int nCoord();

	@Generated
	@StructureField(order = 7, isGetter = false)
	public native void setNCoord(int value);

	@Generated
	@StructureField(order = 8, isGetter = true)
	public native int iLevel();

	@Generated
	@StructureField(order = 8, isGetter = false)
	public native void setILevel(int value);

	@Generated
	@StructureField(order = 9, isGetter = true)
	public native int mxLevel();

	@Generated
	@StructureField(order = 9, isGetter = false)
	public native void setMxLevel(int value);

	@Generated
	@StructureField(order = 10, isGetter = true)
	public native long iRowid();

	@Generated
	@StructureField(order = 10, isGetter = false)
	public native void setIRowid(long value);

	@Generated
	@StructureField(order = 11, isGetter = true)
	public native double rParentScore();

	@Generated
	@StructureField(order = 11, isGetter = false)
	public native void setRParentScore(double value);

	@Generated
	@StructureField(order = 12, isGetter = true)
	public native int eParentWithin();

	@Generated
	@StructureField(order = 12, isGetter = false)
	public native void setEParentWithin(int value);

	@Generated
	@StructureField(order = 13, isGetter = true)
	public native int eWithin();

	@Generated
	@StructureField(order = 13, isGetter = false)
	public native void setEWithin(int value);

	@Generated
	@StructureField(order = 14, isGetter = true)
	public native double rScore();

	@Generated
	@StructureField(order = 14, isGetter = false)
	public native void setRScore(double value);

	@Generated
	@StructureField(order = 15, isGetter = true)
	@ReferenceInfo(type = Void.class, depth = 2)
	public native Ptr<VoidPtr> apSqlParam();

	@Generated
	@StructureField(order = 15, isGetter = false)
	public native void setApSqlParam(Ptr<VoidPtr> value);
}