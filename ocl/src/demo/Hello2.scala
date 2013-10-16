package demo

import com.jogamp.opencl.{CLBuffer, CLDevice, CLContext, CLPlatform}
import java.nio.IntBuffer

object Hello2 extends App{

  val code =
    """
      |kernel void func(global const int* a, global const int* b, global int* c, int numElements) {
      |  // get index into global data array
      |  int iGID = get_global_id(0);
      |
      |  // bound check, equivalent to the limit on a 'for' loop
      |  if (iGID >= numElements)  {
      |    return;
      |  }
      |
      |  // add the vector elements
      |  c[iGID * 2 + 0] = a[iGID * 2 + 0] + b[iGID * 2 + 0]; // = iGID;
      |  c[iGID * 2 + 1] = a[iGID * 2 + 1] + b[iGID * 2 + 1] + 1; // = iGID;
      |}
    """.stripMargin

  val d = CLPlatform.listCLPlatforms()
  d.foreach(println)

  val context = CLContext.create(d(1), CLDevice.Type.GPU)
  try{
    val device = context.getMaxFlopsDevice

    val queue = device.createCommandQueue()

    val program = context.createProgram(code).build()

    println( device.getMaxComputeUnits+"/"+device.getMaxWorkGroupSize)

    val units = device.getMaxComputeUnits
    val size = device.getMaxWorkGroupSize

    def fillBuffer(buffer : CLBuffer[IntBuffer], seed:Int) = {
      val buff = buffer.getBuffer
      while(buff.remaining() != 0) buff.put(seed)
      buff.rewind()
      buffer
    }

    val clBufferA = fillBuffer( context.createIntBuffer(units*size * 2, com.jogamp.opencl.CLMemory.Mem.READ_ONLY), 1 )
    val clBufferB = fillBuffer( context.createIntBuffer(units*size * 2, com.jogamp.opencl.CLMemory.Mem.READ_ONLY), 2 )
    val clBufferC = context.createIntBuffer(units*size * 2, com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY)

    val kernel = program.createCLKernel("func")
    kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(units*size)

    var time = System.nanoTime()

    queue.putWriteBuffer(clBufferA, false).putWriteBuffer(clBufferB, false).put1DRangeKernel(kernel, 0, units*size, size).putReadBuffer(clBufferC, true)

    time = System.nanoTime() - time

    println((time/1000000)+"ms")

    val buff = clBufferC.getBuffer
    var i = 0
    while(buff.remaining() != 0){
      print(buff.get() + " ")
      i += 1
      if(i == 50){
        println()
        i = 0
      }
    }
  }finally {
    context.release()
  }
}
