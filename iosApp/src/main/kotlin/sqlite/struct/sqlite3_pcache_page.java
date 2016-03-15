package sqlite.struct;


import com.intel.inde.moe.natj.c.StructObject;
import com.intel.inde.moe.natj.c.ann.Structure;
import com.intel.inde.moe.natj.c.ann.StructureField;
import com.intel.inde.moe.natj.general.NatJ;
import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Generated;
import com.intel.inde.moe.natj.general.ptr.VoidPtr;

@Generated
@Structure()
public final class sqlite3_pcache_page extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public sqlite3_pcache_page() {
		super(sqlite3_pcache_page.class);
	}

	@Generated
	protected sqlite3_pcache_page(Pointer peer) {
		super(peer);
	}

	@Generated
	public sqlite3_pcache_page(VoidPtr pBuf, VoidPtr pExtra) {
		super(sqlite3_pcache_page.class);
		setPBuf(pBuf);
		setPExtra(pExtra);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	public native VoidPtr pBuf();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setPBuf(VoidPtr value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	public native VoidPtr pExtra();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setPExtra(VoidPtr value);
}