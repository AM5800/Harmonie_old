package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ann.UncertainArgument;
import com.intel.inde.moe.natj.general.ann.UncertainReturn;

@Generated
@Structure()
public final class sqlite3_vtab_cursor extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_vtab_cursor() {
		super(sqlite3_vtab_cursor.class);
	}

	@Generated
	protected sqlite3_vtab_cursor(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_vtab_cursor(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_vtab pVtab) {
		super(sqlite3_vtab_cursor.class);
		setPVtab(pVtab);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	@UncertainReturn("Options: reference, array Fallback: reference")
	public native sqlite3_vtab pVtab();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setPVtab(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_vtab value);
}