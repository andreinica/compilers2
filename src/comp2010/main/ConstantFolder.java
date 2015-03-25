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
		
		int stackCheck = 0;
		InstructionHandle pHandle = null, ppHandle = null;
		Number prevVal = -1, prevPrevVal = -1;
		
		InstructionHandle[] instructionHandles = instList.getInstructionHandles();
		int length = instructionHandles.length;
		int k =0;
		
		for (k = 0; k < length; k++)
		{
			InstructionHandle handle = instructionHandles[k];
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
					stackCheck++;
				}
	
			}
			else if (currentInstruction instanceof BIPUSH) {
				
				BIPUSH BIPUSHInstruction = (BIPUSH)(currentInstruction);
				Number BIPUSHValue= BIPUSHInstruction.getValue();
				
				int value = BIPUSHValue.intValue();
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				stackCheck++;
					
			}
			else if (currentInstruction instanceof SIPUSH) {
				
				SIPUSH SIPUSHInstruction = (SIPUSH)(currentInstruction);
				Number SIPUSHValue= SIPUSHInstruction.getValue();
				
				int value = SIPUSHValue.intValue();
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				stackCheck++;
					
			}
			else if (currentInstruction instanceof ICONST) {
				
				ICONST ICONSTInstruction = (ICONST)(currentInstruction);
				Number ICONSTValue= ICONSTInstruction.getValue();
				
				int value = ICONSTValue.intValue();
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				//stackCheck++;	 WHY DOES THIS GIVE AN ERROR?!?!?!
			}
			else if (currentInstruction instanceof FCONST) {
				
				FCONST FCONSTInstruction = (FCONST)(currentInstruction);
				Number FCONSTValue= FCONSTInstruction.getValue();
				
				float value = FCONSTValue.floatValue();
				
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				stackCheck++;	
			}
			else if (currentInstruction instanceof LCONST) {
				
				LCONST LCONSTInstruction = (LCONST)(currentInstruction);
				Number LCONSTValue= LCONSTInstruction.getValue();
				
				long value = LCONSTValue.longValue();
				
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				stackCheck++;	
			}
			else if (currentInstruction instanceof DCONST) {
				
				DCONST DCONSTInstruction = (DCONST)(currentInstruction);
				Number DCONSTValue= DCONSTInstruction.getValue();
				
				double value = DCONSTValue.doubleValue();
				
				ppHandle = pHandle;
				pHandle = handle;
				prevPrevVal = prevVal;
				prevVal = value;
				stackCheck++;	
			}

			else if (pHandle != null && currentInstruction instanceof ArithmeticInstruction && stackCheck > 1) {
				Number result = 0;
				int check = 0;
				int newCpIndex = 0;
				
				if (currentInstruction instanceof IADD || currentInstruction instanceof ISUB
					|| currentInstruction instanceof IMUL || currentInstruction instanceof IDIV
					|| currentInstruction instanceof IREM) {
					
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
					
					check = 1;
				}
				else if (currentInstruction instanceof FADD || currentInstruction instanceof FSUB
						|| currentInstruction instanceof FMUL || currentInstruction instanceof FDIV
						|| currentInstruction instanceof FREM) {
					
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
					
					check = 2;
				}
				else if (currentInstruction instanceof LADD || currentInstruction instanceof LSUB
						|| currentInstruction instanceof LMUL || currentInstruction instanceof LDIV
						|| currentInstruction instanceof LREM) {
					
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
					
					check = 3;
				}
				else if (currentInstruction instanceof DADD || currentInstruction instanceof DSUB
						|| currentInstruction instanceof DMUL || currentInstruction instanceof DDIV
						|| currentInstruction instanceof DREM) {
					
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
					
					check = 4;
				}
				
				if (check > 0) {
					InstructionHandle newHandle;
					switch (check) {
						case 1: //integer
							/*int val = result.intValue();
							if (val >= -1 && val <= 5) {
								newHandle = instList.append(handle, new ICONST((byte)(val)));
							}
							else if (val >= -128 && val <= 127) {
								newHandle = instList.append(handle, new BIPUSH((byte)(val)));
							}
							else if (val >= -32768 && val <= 32767) {
								newHandle = instList.append(handle, new SIPUSH((short)(val)));
							}
							else {
								newCpIndex = cpgen.addInteger(val);
								newHandle = instList.append(handle, new LDC(newCpIndex));
							}*/
							newHandle = instList.append(handle, new PUSH(cpgen, result.intValue()));
							break;
							
						case 2: //float
							//newCpIndex = cpgen.addFloat(result.floatValue());;
							//newHandle = instList.append(handle, new LDC_W(newCpIndex));
							newHandle = instList.append(handle, new PUSH(cpgen, result.floatValue()));
							break;
							
						case 3: //long
							//newCpIndex = cpgen.addLong(result.longValue());
							//newHandle = instList.append(handle, new LDC2_W(newCpIndex));
							newHandle = instList.append(handle, new PUSH(cpgen, result.longValue()));
							break;
							
						case 4: //double
							//newCpIndex = cpgen.addDouble(result.doubleValue());
							//newHandle = instList.append(handle, new LDC2_W(newCpIndex));
							newHandle = instList.append(handle, new PUSH(cpgen, result.doubleValue()));
							break;
							
						default:
							newHandle = handle;
							break;
					}
					
					
					
					try {
						instList.delete(pHandle);
						if (ppHandle != null) {
							instList.delete(ppHandle);
						}
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
					     InstructionHandle[] targets = e.getTargets();

					     for(int i=0; i < targets.length; i++) {
					          InstructionTargeter[] targeters = targets[i].getTargeters();

					          for(int j=0; j < targeters.length; j++)
					               targeters[j].updateTarget(targets[i], handle);
					     }
					}
					pHandle = newHandle;
					ppHandle = null;
					prevPrevVal = prevVal;
					prevVal = result;
					stackCheck++;
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
