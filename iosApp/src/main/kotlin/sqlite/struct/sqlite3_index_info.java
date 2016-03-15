package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ann.UncertainArgument;
import com.intel.inde.moe.natj.general.ann.UncertainReturn;
import com.intel.inde.moe.natj.general.ptr.BytePtr;

@Generated
@Structure()
public final class sqlite3_index_info extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_index_info() {
		super(sqlite3_index_info.class);
	}

	@Generated
	protected sqlite3_index_info(Pointer peer) {
		super(peer);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native int nConstraint();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setNConstraint(int value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	@UncertainReturn("Options: reference, array Fallback: reference")
	public native sqlite3_index_constraint aConstraint();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setAConstraint(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_index_constraint value);

	@Generated
	@StructureField(order = 2, isGetter = true)
	public native int nOrderBy();

	@Generated
	@StructureField(order = 2, isGetter = false)
	public native void setNOrderBy(int value);

	@Generated
	@StructureField(order = 3, isGetter = true)
	@UncertainReturn("Options: reference, array Fallback: reference")
	public native sqlite3_index_orderby aOrderBy();

	@Generated
	@StructureField(order = 3, isGetter = false)
	public native void setAOrderBy(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_index_orderby value);

	@Generated
	@StructureField(order = 4, isGetter = true)
	@UncertainReturn("Options: reference, array Fallback: reference")
	public native sqlite3_index_constraint_usage aConstraintUsage();

	@Generated
	@StructureField(order = 4, isGetter = false)
	public native void setAConstraintUsage(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_index_constraint_usage value);

	@Generated
	@StructureField(order = 5, isGetter = true)
	public native int idxNum();

	@Generated
	@StructureField(order = 5, isGetter = false)
	public native void setIdxNum(int value);

	@Generated
	@StructureField(order = 6, isGetter = true)
	public native BytePtr idxStr();

	@Generated
	@StructureField(order = 6, isGetter = false)
	public native void setIdxStr(BytePtr value);

	@Generated
	@StructureField(order = 7, isGetter = true)
	public native int needToFreeIdxStr();

	@Generated
	@StructureField(order = 7, isGetter = false)
	public native void setNeedToFreeIdxStr(int value);

	@Generated
	@StructureField(order = 8, isGetter = true)
	public native int orderByConsumed();

	@Generated
	@StructureField(order = 8, isGetter = false)
	public native void setOrderByConsumed(int value);

	@Generated
	@StructureField(order = 9, isGetter = true)
	public native double estimatedCost();

	@Generated
	@StructureField(order = 9, isGetter = false)
	public native void setEstimatedCost(double value);

	@Generated
	@StructureField(order = 10, isGetter = true)
	public native long estimatedRows();

	@Generated
	@StructureField(order = 10, isGetter = false)
	public native void setEstimatedRows(long value);

	@Generated
	@StructureField(order = 11, isGetter = true)
	public native int idxFlags();

	@Generated
	@StructureField(order = 11, isGetter = false)
	public native void setIdxFlags(int value);

	@Generated
	@StructureField(order = 12, isGetter = true)
	public native long colUsed();

	@Generated
	@StructureField(order = 12, isGetter = false)
	public native void setColUsed(long value);
}