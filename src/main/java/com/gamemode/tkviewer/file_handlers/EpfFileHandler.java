package com.gamemode.tkviewer.file_handlers;

//import java.awt.*;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Stencil;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

public class EpfFileHandler extends FileHandler {

    public static final int HEADER_SIZE = 0xC;
    public static final int FRAME_SIZE = 0x10;

    public static int dataSize = 0;
    public static boolean allFramesLoaded = false;

    public int frameCount;
    public int width;
    public int height;
    public int bitBLT;
    public long pixelDataLength;
    public Map<Integer, Frame> frames_map;

    public EpfFileHandler(String filepath) {
        this(new File(filepath), false);
    }

    public EpfFileHandler(File file) {
        this(file, false);
    }

    public EpfFileHandler(String filepath, boolean loadAllFrames) {
        this(new File(filepath), loadAllFrames);
    }

    public EpfFileHandler(File file, boolean loadAllFrames) {
        super(file);

        frames_map = new HashMap<Integer, Frame>();

        this.frameCount = this.readShort(true, false);
        this.width = this.readShort(true, false);
        this.height = this.readShort(true, false);
        this.bitBLT = this.readShort(true, false);
        this.pixelDataLength = this.readInt(true, true);

        if (loadAllFrames) {
            this.loadAllFrames();
        }
    }

    public Frame getFrame(int index) {
        // Return Frame if Cached
        if (this.frames_map.containsKey(index)) {
            return this.frames_map.get(index);
        }

        // Seek to Frame and read
        this.seek((HEADER_SIZE + this.pixelDataLength + (index * FRAME_SIZE)), true);
        int top = this.readShort(true, false);
        int left = this.readShort(true, false);
        int bottom = this.readShort(true, false);
        int right = this.readShort(true, false);

        int width = (right - left);
        int height = (bottom - top);

        long pixelDataOffset = this.readInt(true, true);
        long stencilDataOffset = this.readInt(true, true);

        // Seek to Pixel Data and Stencil Data
        this.seek(HEADER_SIZE + pixelDataOffset, true);
        ByteBuffer rawPixelData = this.readBytes((width * height), true);
        dataSize += rawPixelData.capacity();
        Stencil stencil = new Stencil(this, stencilDataOffset, new Dimension(width, height));
        ByteBuffer rawStencilData = stencil.toByteBuffer();
        dataSize += rawStencilData.capacity();

        Frame frame = new Frame(top, left, bottom, right, width, height, pixelDataOffset, stencilDataOffset, rawPixelData, rawStencilData, stencil);
        this.frames_map.put(index, frame);

        return this.frames_map.get(index);
    }

    /*
     * Loads all Frames in the EPF to frames_map.
     */
    public void loadAllFrames() {
        for (int i = 0; i < this.frameCount; i++) {
            this.getFrame(i);
        }
        allFramesLoaded = true;
    }

    public ByteBuffer toByteBuffer() {
        if (!allFramesLoaded) {
            this.loadAllFrames();
        }

        ByteBuffer epfBytes = ByteBuffer.allocate(HEADER_SIZE + (FRAME_SIZE * this.frameCount) + dataSize);
        epfBytes.order(ByteOrder.LITTLE_ENDIAN);

        // Header
        epfBytes.putShort((short)this.frameCount);
        epfBytes.putShort((short)this.width);
        epfBytes.putShort((short)this.height);
        epfBytes.putShort((short)this.bitBLT);
        epfBytes.putInt((int)this.pixelDataLength);

        // Frames (Pixel Data)
        for (int i = 0; i < this.frames_map.size(); i++) {
            Frame frame = this.frames_map.get(i);
            epfBytes.put(frame.getRawPixelData());
            //System.out.println(epfBytes.position());
        }

        // Frames (Stencil Data)
        for (int i = 0; i < this.frames_map.size(); i++) {
            Frame frame = this.frames_map.get(i);
            ByteBuffer stencilData = frame.getRawStencilData();
            stencilData.order(ByteOrder.LITTLE_ENDIAN);
            epfBytes.put(stencilData);
            //System.out.println(epfBytes.position());
        }

        // Frames (TOC)
        for (int i = 0; i < this.frames_map.size(); i++) {
            Frame frame = this.frames_map.get(i);

            epfBytes.putShort((short)frame.getTop());
            epfBytes.putShort((short)frame.getLeft());
            epfBytes.putShort((short)frame.getBottom());
            epfBytes.putShort((short)frame.getRight());
            epfBytes.putInt((int)frame.getPixelDataOffset());
            //System.out.println("1: " + epfBytes.position());
            epfBytes.putInt((int)frame.getStencilDataOffset());
            //System.out.println("2: " + epfBytes.position());
        }

        return epfBytes;
    }
}
