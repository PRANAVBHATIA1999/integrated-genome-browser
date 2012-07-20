package com.affymetrix.igb.shared;

import java.util.HashMap;
import java.util.Map;

public enum AminoAcid {
	Alanine("Ala", 'A'),
	Arginine("Arg", 'R'),
	Asparagine("Asn", 'N'),
	AsparticAcid("Asp", 'D'),
	Cysteine("Cys", 'C'),
	GlutamicAcid("Glu", 'E'),
	Glutamine("Gln", 'Q'),
	Glycine("Gly", 'G'),
	Histidine("His", 'H'),
	Isoleucine("Ile", 'I'),
	Leucine("Leu", 'L'),
	Lysine("Lys", 'K'),
	Methionine("Met", 'M'),
	Phenylalanine("Phe", 'F'),
	Proline("Pro", 'P'),
	Serine("Ser", 'S'),
	Threonine("Thr", 'T'),
	Tryptophan("Trp", 'W'),
	Tyrosine("Tyr", 'Y'),
	Valine("Val", 'V'),
//	Selenocysteine("Sec", 'U'),
//	Pyrrolysine("Pyl", 'O'),
	STOP("---", '-');

	public static final String START_CODON = "ATG"; //AminoAcid.Methionine - not true for all species
	public static final String[] STOP_CODONS = new String[]{"TAA", "TAG", "TGA"};
	public static HashMap<String, AminoAcid> CODON_TO_AMINO_ACID = new HashMap<String, AminoAcid>();
	static {
		CODON_TO_AMINO_ACID.put("AAA", AminoAcid.Lysine);
		CODON_TO_AMINO_ACID.put("AAC", AminoAcid.Asparagine);
		CODON_TO_AMINO_ACID.put("AAG", AminoAcid.Lysine);
		CODON_TO_AMINO_ACID.put("AAT", AminoAcid.Asparagine);
		CODON_TO_AMINO_ACID.put("ACA", AminoAcid.Threonine);
		CODON_TO_AMINO_ACID.put("ACC", AminoAcid.Threonine);
		CODON_TO_AMINO_ACID.put("ACG", AminoAcid.Threonine);
		CODON_TO_AMINO_ACID.put("ACT", AminoAcid.Threonine);
		CODON_TO_AMINO_ACID.put("AGA", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("AGC", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("AGG", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("AGT", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("ATA", AminoAcid.Isoleucine);
		CODON_TO_AMINO_ACID.put("ATC", AminoAcid.Isoleucine);
		CODON_TO_AMINO_ACID.put("ATG", AminoAcid.Methionine);
		CODON_TO_AMINO_ACID.put("ATT", AminoAcid.Isoleucine);
		CODON_TO_AMINO_ACID.put("CAA", AminoAcid.Glutamine);
		CODON_TO_AMINO_ACID.put("CAC", AminoAcid.Histidine);
		CODON_TO_AMINO_ACID.put("CAG", AminoAcid.Glutamine);
		CODON_TO_AMINO_ACID.put("CAT", AminoAcid.Histidine);
		CODON_TO_AMINO_ACID.put("CCA", AminoAcid.Proline);
		CODON_TO_AMINO_ACID.put("CCC", AminoAcid.Proline);
		CODON_TO_AMINO_ACID.put("CCG", AminoAcid.Proline);
		CODON_TO_AMINO_ACID.put("CCT", AminoAcid.Proline);
		CODON_TO_AMINO_ACID.put("CGA", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("CGC", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("CGG", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("CGT", AminoAcid.Arginine);
		CODON_TO_AMINO_ACID.put("CTA", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("CTC", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("CTG", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("CTT", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("GAA", AminoAcid.GlutamicAcid);
		CODON_TO_AMINO_ACID.put("GAC", AminoAcid.AsparticAcid);
		CODON_TO_AMINO_ACID.put("GAG", AminoAcid.GlutamicAcid);
		CODON_TO_AMINO_ACID.put("GAT", AminoAcid.AsparticAcid);
		CODON_TO_AMINO_ACID.put("GCA", AminoAcid.Alanine);
		CODON_TO_AMINO_ACID.put("GCC", AminoAcid.Alanine);
		CODON_TO_AMINO_ACID.put("GCG", AminoAcid.Alanine);
		CODON_TO_AMINO_ACID.put("GCT", AminoAcid.Alanine);
		CODON_TO_AMINO_ACID.put("GGA", AminoAcid.Glycine);
		CODON_TO_AMINO_ACID.put("GGC", AminoAcid.Glycine);
		CODON_TO_AMINO_ACID.put("GGG", AminoAcid.Glycine);
		CODON_TO_AMINO_ACID.put("GGT", AminoAcid.Glycine);
		CODON_TO_AMINO_ACID.put("GTA", AminoAcid.Valine);
		CODON_TO_AMINO_ACID.put("GTC", AminoAcid.Valine);
		CODON_TO_AMINO_ACID.put("GTG", AminoAcid.Valine);
		CODON_TO_AMINO_ACID.put("GTT", AminoAcid.Valine);
		CODON_TO_AMINO_ACID.put("TAA", AminoAcid.STOP);
		CODON_TO_AMINO_ACID.put("TAC", AminoAcid.Tyrosine);
		CODON_TO_AMINO_ACID.put("TAG", AminoAcid.STOP); // AminoAcid.Pyrrolysine
		CODON_TO_AMINO_ACID.put("TAT", AminoAcid.Tyrosine);
		CODON_TO_AMINO_ACID.put("TCA", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("TCC", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("TCG", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("TCT", AminoAcid.Serine);
		CODON_TO_AMINO_ACID.put("TGA", AminoAcid.STOP); // AminoAcid.Selenocysteine
		CODON_TO_AMINO_ACID.put("TGC", AminoAcid.Cysteine);
		CODON_TO_AMINO_ACID.put("TGG", AminoAcid.Tryptophan);
		CODON_TO_AMINO_ACID.put("TGT", AminoAcid.Cysteine);
		CODON_TO_AMINO_ACID.put("TTA", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("TTC", AminoAcid.Phenylalanine);
		CODON_TO_AMINO_ACID.put("TTG", AminoAcid.Leucine);
		CODON_TO_AMINO_ACID.put("TTT", AminoAcid.Phenylalanine);
	}

	private static final Map<Character, AminoAcid> code1aminoAcid = new HashMap<Character, AminoAcid>();
	static {
		code1aminoAcid.put('A', Alanine);
		code1aminoAcid.put('R', Arginine);
		code1aminoAcid.put('N', Asparagine);
		code1aminoAcid.put('D', AsparticAcid);
		code1aminoAcid.put('C', Cysteine);
		code1aminoAcid.put('E', GlutamicAcid);
		code1aminoAcid.put('Q', Glutamine);
		code1aminoAcid.put('G', Glycine);
		code1aminoAcid.put('H', Histidine);
		code1aminoAcid.put('I', Isoleucine);
		code1aminoAcid.put('L', Leucine);
		code1aminoAcid.put('K', Lysine);
		code1aminoAcid.put('M', Methionine);
		code1aminoAcid.put('F', Phenylalanine);
		code1aminoAcid.put('P', Proline);
		code1aminoAcid.put('S', Serine);
		code1aminoAcid.put('T', Threonine);
		code1aminoAcid.put('W', Tryptophan);
		code1aminoAcid.put('Y', Tyrosine);
		code1aminoAcid.put('V', Valine);
		code1aminoAcid.put('-', STOP);
	}
	private static final Map<String, AminoAcid> code3aminoAcid = new HashMap<String, AminoAcid>();
	static {
		code3aminoAcid.put("Ala", Alanine);
		code3aminoAcid.put("Arg", Arginine);
		code3aminoAcid.put("Asn", Asparagine);
		code3aminoAcid.put("Asp", AsparticAcid);
		code3aminoAcid.put("Cys", Cysteine);
		code3aminoAcid.put("Glu", GlutamicAcid);
		code3aminoAcid.put("Gln", Glutamine);
		code3aminoAcid.put("Gly", Glycine);
		code3aminoAcid.put("His", Histidine);
		code3aminoAcid.put("Ile", Isoleucine);
		code3aminoAcid.put("Leu", Leucine);
		code3aminoAcid.put("Lys", Lysine);
		code3aminoAcid.put("Met", Methionine);
		code3aminoAcid.put("Phe", Phenylalanine);
		code3aminoAcid.put("Pro", Proline);
		code3aminoAcid.put("Ser", Serine);
		code3aminoAcid.put("Thr", Threonine);
		code3aminoAcid.put("Trp", Tryptophan);
		code3aminoAcid.put("Tyr", Tyrosine);
		code3aminoAcid.put("Val", Valine);
		code3aminoAcid.put("---", STOP);
	}
	private final String code;
	private final char letter;
	private AminoAcid(String code, char letter) {
		this.code = code;
		this.letter = letter;
	}
	public String getCode() {
		return code;
	}
	public char getLetter() {
		return letter;
	}
	public static AminoAcid getByCode(String code) {
		return code3aminoAcid.get(code.substring(0, 1).toUpperCase() + code.substring(1, 3).toLowerCase());
	}
}
