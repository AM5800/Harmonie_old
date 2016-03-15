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
public final class Fts5PhraseIter extends StructObject {
	static {
		NatJ.register();
	}
	private static long __natjCache;

	@Generated
	public Fts5PhraseIter() {
		super(Fts5PhraseIter.class);
	}

	@Generated
	protected Fts5PhraseIter(Pointer peer) {
		super(peer);
	}

	@Generated
	public Fts5PhraseIter(
			@UncertainArgument("Options: java.string, c.const-byte-ptr Fallback: java.string") String a,
			@UncertainArgument("Options: java.string, c.const-byte-ptr Fallback: java.string") String b) {
		super(Fts5PhraseIter.class);
		setA(a);
		setB(b);
	}

	@Generated
	@StructureField(order = 0, isGetter = true)
	@UncertainReturn("Options: java.string, c.const-byte-ptr Fallback: java.string")
	public native String a();

	@Generated
	@StructureField(order = 0, isGetter = false)
	public native void setA(
			@UncertainArgument("Options: java.string, c.const-byte-ptr Fallback: java.string") String value);

	@Generated
	@StructureField(order = 1, isGetter = true)
	@UncertainReturn("Options: java.string, c.const-byte-ptr Fallback: java.string")
	public native String b();

	@Generated
	@StructureField(order = 1, isGetter = false)
	public native void setB(
			@UncertainArgument("Options: java.string, c.const-byte-ptr Fallback: java.string") String value);
}