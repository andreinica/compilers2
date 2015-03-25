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
		

		int prevPrevHandle = 0, prevHandle = 0;
		int prevValue = -1, prevprevValue = -1;
		InstructionHandle pHandle = null, ppHandle = null;
		int i = 0;
		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			Instruction currentInstruction = handle.getInstruction();
			i++;
			
			
			if (currentInstruction instanceof LDC) {
				int instructionIndex = handle.getPosition();
				LDC LDCInstruction = (LDC)(currentInstruction);
				int cpIndex = LDCInstruction.getIndex();
				
				Object LDCValue = LDCInstruction.getValue(cpgen);
				if (LDCValue instanceof Integer) {
					int value = (int)(LDCValue);
					ppHandle = pHandle;
					pHandle = handle;
					prevPrevHandle = prevHandle;
					prevHandle = instructionIndex;
					prevprevValue = prevValue;
					prevValue = value;
					cpgen.addString(instructionIndex + "_" + cpIndex + "_" + value + "_suck_" + i + ": " + currentInstruction.toString());
				}
				
			}
			else {
				if (currentInstruction instanceof IADD && pHandle != null && ppHandle != null) {
					
					
					int newCpIndex = cpgen.addInteger(prevValue + prevprevValue);
					instList.insert(handle, new LDC(newCpIndex));
					try {
						instList.delete(pHandle);
						instList.delete(ppHandle);
						instList.delete(handle);
					}
					catch (TargetLostException e)
					{
						e.printStackTrace();
					}
					
					
				}
			}
			
			
			/*if (currentInstruction instanceof LDC) 
			{
				prevPrevHandle = prevHandle;
				prevHandle = handle.getPosition();
				
				int pindex = ((LDC)(currentInstruction)).getIndex();
				//int ppindex = pindex;
				
				
				
				Constant consta = cpgen.getConstant(pindex);
				if (consta instanceof ConstantInteger){
					ConstantInteger prevIndexCI = (ConstantInteger) (consta);
					prevprevValue = prevValue;
					ConstantPool cp = cpgen.getConstantPool();
					cpgen.addString("LDC + " + currentInstruction.toString());
					prevValue = Integer.parseInt((prevIndexCI.getConstantValue(cp)).toString());
					//cpgen.addInteger(prevValue);
					
				}
				else {
					cpgen.addString("suck + " + currentInstruction.toString());
				}
				
				
				
				
			}
			else if (currentInstruction instanceof ArithmeticInstruction)
			{			
			
				//instList.insert(handle, new LDC(index));
				
				int sum = prevValue + prevprevValue;
				
				instList.insert(handle, new LDC(prevValue));
				//instList.insert(handle, new LDC(prevprevValue));
				instList.insert(handle, new LDC(sum));
				//cpgen.addInteger(asd);
				
				//cpgen.addInteger(prevValue);
				//cpgen.addInteger(prevprevValue - 10);
				//cpgen.addInteger(sum);

				try
				{
					instList.delete(handle);
					//instList.delete(instList.findHandle(prevHandle));
					//instList.delete(prev);
					//instList.delete(prevPrev);
				}
				catch (TargetLostException e)
				{
					e.printStackTrace();
				}
				
				
			}
			
		_*/	
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
