# USBKit

**what:**
<br>hacker boy tries to connect nexus 6p to [op-1](https://www.teenageengineering.com/products/op-1)

**why:**
<br>[chill beats](https://op1.fun), and [libaums](https://github.com/magnusja/libaums) doesn't support FAT16

**how:**
<br>matthias treydte's [fat32-lib](https://github.com/waldheinz/fat32-lib)

**will it work:**
<br>let's see

**license:**
<br>MIT (edit:...might end up [GPL](https://github.com/magnusja/libaums/issues/72#issuecomment-296774149) if test 3 works out)

**ways to go:**
1. use getExternalFilesDir for the disk, get root of that, then pass the File to fat32-lib
2. use UsbManager, use openAccessory, convert ParcelFileDescriptor to File somehow...?
3. use https://github.com/alt236/USB-Device-Info---Android to get usb device path -> File
4. use UsbDevice and go through the UsbInterfaces and UsbEndpoints for clues
5. use getExternalFilesDir, get root of given path, request File for root path

**ways to go, mark two:**
1. find a path, make a file, pass to fat32-lib
2. find the device, go from there

**passes:**
1. test 1, find a path: getExternalFilesDir
2. test 2, find the device: UsbManager
3. test 3, libaums with jnode fs implementation set

**hail mary's:**
1. usbdevice -> usbdeviceconnection -> getfiledescriptor -> fileinputstream -> getfilechannel, modify FileDisk to use raw filechannels instead of only from File

**final update**
got FAT16 reads working with test 3, using libaums + jnode-fs. nice. no need for this then.
