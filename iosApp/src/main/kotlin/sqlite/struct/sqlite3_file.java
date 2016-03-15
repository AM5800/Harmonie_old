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
public final class sqlite3_file extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_file() {
		super(sqlite3_file.class);
	}

	@Generated
	protected sqlite3_file(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_file(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_io_methods pMethods) {
		super(sqlite3_file.class);
		setPMethods(pMethods);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	@UncertainReturn("Options: reference, array Fallback: reference")
	public native sqlite3_io_methods pMethods();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setPMethods(
			@UncertainArgument("Options: reference, array Fallback: reference") sqlite3_io_methods value);
}