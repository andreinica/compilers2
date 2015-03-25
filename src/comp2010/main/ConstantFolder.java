package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		
		Code methodCode = method.getCode();
		InstructionList instList = new InstructionList(methodCode.getCode());
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);
		

		InstructionHandle pHandle = null, ppHandle = null;
		Number prevVal = -1, prevPrevVal = -1;

		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			Instruction currentInstruction = handle.getInstruction();
			
			if (currentInstruction instanceof CPInstruction) {
				Number value = 0;
				CPInstruction instruction = (CPInstruction)(currentInstruction);
				int check = 0;
				if (instruction instanceof LDC) {
					LDC LDCInstruction = (LDC)(instruction);
					Object LDCValue = LDCInstruction.getValue(cpgen);
					if (LDCValue instanceof Integer) {
						value = (int)(LDCValue);
						check = 1;
					}
					else if (LDCValue instanceof Float) {
						value = (float)(LDCValue);
						check = 1;
					}
				}
				else if (instruction instanceof LDC_W) {
					LDC_W LDC_WInstruction = (LDC_W)(currentInstruction);
					Object LDC_WValue = LDC_WInstruction.getValue(cpgen);
					if (LDC_WValue instanceof Integer) {
						value = (int)(LDC_WValue);
						check = 1;
					}
					else if (LDC_WValue instanceof Float) {
						value = (float)(LDC_WValue);
						check = 1;
					}
				}
				else if (instruction instanceof LDC2_W) {
					LDC2_W LDC2_WInstruction = (LDC2_W)(currentInstruction);
					Object LDC2_WValue = LDC2_WInstruction.getValue(cpgen);
					if (LDC2_WValue instanceof Long) {
						value = (long)(LDC2_WValue);
						check = 1;
					}
					else if (LDC2_WValue instanceof Double) {
						value = (double)(LDC2_WValue);
						check = 1;
					}
				}

				if (check == 1) {
					ppHandle = pHandle;
					pHandle = handle;
					prevPrevVal = prevVal;
					prevVal = value;
				}
	
			}

			else if (pHandle != null && currentInstruction instanceof ArithmeticInstruction) {
				if (currentInstruction instanceof IADD || currentInstruction instanceof ISUB
					|| currentInstruction instanceof IMUL || currentInstruction instanceof IDIV
					|| currentInstruction instanceof IREM) {
					
					
					int result = 0;
					
					int a = prevPrevVal.intValue();
					int b = prevVal.intValue();
					
					if (currentInstruction instanceof IADD){
						result = a + b;
					}
					else if (currentInstruction instanceof ISUB){
						result = a - b;
					}
					else if (currentInstruction instanceof IMUL){
						result = a * b;
					}
					else if (currentInstruction instanceof IDIV){
						result = a / b;
					}
					else if (currentInstruction instanceof IREM){
						result = a % b;
					}
					
					int newCpIndex = cpgen.addInteger(result);;
					InstructionHandle newHandle = instList.append(handle, new LDC(newCpIndex));
					
					try {
						instList.delete(pHandle);
						if (ppHandle != null) {
							instList.delete(ppHandle);
						}
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
						e.printStackTrace();
					}
					
					pHandle = newHandle;
					ppHandle = null;
					prevPrevVal = prevVal;
					prevVal = result;
				}
				else if (currentInstruction instanceof FADD || currentInstruction instanceof FSUB
						|| currentInstruction instanceof FMUL || currentInstruction instanceof FDIV
						|| currentInstruction instanceof FREM) {
						
					float result = 0;
					
					float a = prevPrevVal.floatValue();
					float b = prevVal.floatValue();
					
					if (currentInstruction instanceof FADD){
						result = a + b;
					}
					else if (currentInstruction instanceof FSUB){
						result = a - b;
					}
					else if (currentInstruction instanceof FMUL){
						result = a * b;
					}
					else if (currentInstruction instanceof FDIV){
						result = a / b;
					}
					else if (currentInstruction instanceof FREM){
						result = a % b;
					}
					
					int newCpIndex = cpgen.addFloat(result);;
					InstructionHandle newHandle = instList.append(handle, new LDC_W(newCpIndex));
					
					try {
						instList.delete(pHandle);
						if (ppHandle != null) {
							instList.delete(ppHandle);
						}
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
						e.printStackTrace();
					}
					
					pHandle = newHandle;
					ppHandle = null;
					prevPrevVal = prevVal;
					prevVal = result;
				}
				else if (currentInstruction instanceof LADD || currentInstruction instanceof LSUB
						|| currentInstruction instanceof LMUL || currentInstruction instanceof LDIV
						|| currentInstruction instanceof LREM) {
						
					long result = 0;
					
					long a = prevPrevVal.longValue();
					long b = prevVal.longValue();
					
					if (currentInstruction instanceof LADD){
						result = a + b;
					}
					else if (currentInstruction instanceof LSUB){
						result = a - b;
					}
					else if (currentInstruction instanceof LMUL){
						result = a * b;
					}
					else if (currentInstruction instanceof LDIV){
						result = a / b;
					}
					else if (currentInstruction instanceof LREM){
						result = a % b;
					}
					
					int newCpIndex = cpgen.addLong(result);
					InstructionHandle newHandle = instList.append(handle, new LDC2_W(newCpIndex));
					
					try {
						instList.delete(pHandle);
						if (ppHandle != null) {
							instList.delete(ppHandle);
						}
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
						e.printStackTrace();
					}
					
					pHandle = newHandle;
					ppHandle = null;
					prevPrevVal = prevVal;
					prevVal = result;
				}
				else if (currentInstruction instanceof DADD || currentInstruction instanceof DSUB
						|| currentInstruction instanceof DMUL || currentInstruction instanceof DDIV
						|| currentInstruction instanceof DREM) {
						
					double result = 0;
					
					double a = prevPrevVal.doubleValue();
					double b = prevVal.doubleValue();
					
					if (currentInstruction instanceof DADD){
						result = a + b;
					}
					else if (currentInstruction instanceof DSUB){
						result = a - b;
					}
					else if (currentInstruction instanceof DMUL){
						result = a * b;
					}
					else if (currentInstruction instanceof DDIV){
						result = a / b;
					}
					else if (currentInstruction instanceof DREM){
						result = a % b;
					}
					
					int newCpIndex = cpgen.addDouble(result);;
					InstructionHandle newHandle = instList.append(handle, new LDC2_W(newCpIndex));
					
					try {
						instList.delete(pHandle);
						if (ppHandle != null) {
							instList.delete(ppHandle);
						}
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
						e.printStackTrace();
					}
					
					pHandle = newHandle;
					ppHandle = null;
					prevPrevVal = prevVal;
					prevVal = result;
				}
			}	
		}
		
		// setPositions(true) checks whether jump handles 
		// are all within the current method
		instList.setPositions(true);

		// set max stack/local
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// generate the new method with replaced iconst
		Method newMethod = methodGen.getMethod();
		// replace the method in the original class
		cgen.replaceMethod(method, newMethod);

	}

	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		
		// Do your optimization here
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Do your optimization here
		Method[] methods = cgen.getMethods();
		for (Method m : methods)
		{
			optimizeMethod(cgen, cpgen, m);

		}

		this.optimized = cgen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
