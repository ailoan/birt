/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.core.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import com.ibm.icu.text.SimpleDateFormat;

public class ArchiveUtil
{

	// We need this because the report document should be platform neutual. Here
	// we define the neutual is the unix seperator.
	public static String UNIX_SEPERATOR = "/";

	/**
	 * @param rootPath -
	 *            the absolute path of the root folder. The path is seperated by
	 *            system's File seperator.
	 * @param relativePath -
	 *            the relative path. The path is either seperated by system's
	 *            File seperator or seperated by Unix seperator "/".
	 * @return the absolute path which concats rootPath and relativePath. The
	 *         full path is seperated by system's File seperator. The returned
	 *         absolute path can be used directly to locate the file.
	 */
	public static String generateFullPath( String rootPath, String relativePath )
	{
		relativePath = convertToSystemString( relativePath );

		if ( rootPath != null )
		{
			if ( !rootPath.endsWith( File.separator ) )
				rootPath += File.separator;

			if ( relativePath.startsWith( File.separator ) )
				relativePath = relativePath.substring( 1 );

			return rootPath + relativePath;
		}

		return relativePath;
	}

	/**
	 * @param rootPath -
	 *            the absolute path of the root folder. The path is seperated by
	 *            system's File seperator.
	 * @param fullString -
	 *            the absolute path of the stream. The path is seperated by
	 *            system's File seperator.
	 * @return the relative path string. The path is based on Unix syntax and
	 *         starts with "/".
	 */
	public static String generateRelativePath( String rootPath, String fullPath )
	{
		String relativePath = null;

		if ( ( rootPath != null ) && fullPath.startsWith( rootPath ) )
		{
			relativePath = fullPath.substring( rootPath.length( ) );
		}
		else
			relativePath = fullPath;

		relativePath = convertToUnixString( relativePath );

		if ( !relativePath.startsWith( UNIX_SEPERATOR ) )
			relativePath = UNIX_SEPERATOR + relativePath;

		return relativePath;
	}

	/**
	 * @param path -
	 *            the path that could be in system format (seperated by
	 *            File.seperator) or Unix format (seperated by "/").
	 * @return the path that is in Unix format.
	 */
	private static String convertToUnixString( String path )
	{
		if ( path == null )
			return null;

		return path.replace( File.separator.charAt( 0 ), UNIX_SEPERATOR
				.charAt( 0 ) );
	}

	/**
	 * @param path -
	 *            the path that could be in system format (seperated by
	 *            File.seperator) or Unix format (seperated by "/").
	 * @return the path that is in the system format.
	 */
	private static String convertToSystemString( String path )
	{
		if ( path == null )
			return null;

		return path.replace( UNIX_SEPERATOR.charAt( 0 ), File.separator
				.charAt( 0 ) );
	}

	/**
	 * Generate a unique file or folder name which is in the same folder as the
	 * originalName
	 * 
	 * @param originalName -
	 *            the original Name. For example, it could be the name of the
	 *            file archive
	 * @return a unique file or folder name which is in the same folder as the
	 *         originalName
	 */
	synchronized public static String generateUniqueFileFolderName(
			String originalName )
	{
		SimpleDateFormat df = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" ); //$NON-NLS-1$
		String dateTimeString = df.format( new Date( ) );
		String folderName = originalName + "_" + dateTimeString; //$NON-NLS-1$

		Random generator = new Random( );
		File folder = new File( folderName );
		while ( folder.exists( ) )
		{
			folderName += generator.nextInt( );
			folder = new File( folderName );
		}

		return folderName;
	}

	/**
	 * If the parent folder of the file doesn't exsit, create the parent folder.
	 */
	public static void createParentFolder( File fd )
	{
		if ( fd != null && fd.getParentFile( ) != null
				&& fd.getParentFile( ).exists( ) == false )
		{
			fd.getParentFile( ).mkdirs( );
		}
	}

	/**
	 * Recursively delete all the files and folders under dirOrFile
	 * 
	 * @param dirOrFile -
	 *            the File object which could be either a folder or a file.
	 */
	public static void DeleteAllFiles( File dirOrFile )
	{
		if ( !dirOrFile.exists( ) )
			return;

		if ( dirOrFile.isFile( ) )
		{
			dirOrFile.delete( );
		}
		else
		// dirOrFile is directory
		{
			if ( ( dirOrFile.listFiles( ) != null )
					&& ( dirOrFile.listFiles( ).length > 0 ) )
			{
				File[] fileList = dirOrFile.listFiles( );
				for ( int i = 0; i < fileList.length; i++ )
					DeleteAllFiles( fileList[i] );
			}

			// Directory can only be deleted when it is empty.
			dirOrFile.delete( );
		}
	}

	public static void zipFolderToStream( String tempFolderPath,
			OutputStream ostream )
	{
		ZipOutputStream zipOutput = new ZipOutputStream( ostream );
		File rootDir = new File( tempFolderPath );
		File[] files = rootDir.listFiles( );

		try
		{
			zipFiles( zipOutput, files, tempFolderPath );
			zipOutput.close( );
		}
		catch ( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}

	/**
	 * Utility funtion to write files/directories to a ZipOutputStream. For
	 * directories, all the files and subfolders are written recursively.
	 */
	private static void zipFiles( ZipOutputStream zipOut, File[] files,
			String tempFolderPath ) throws FileNotFoundException, IOException
	{
		if ( files == null )
			return;

		for ( int i = 0; i < files.length; i++ )
		{
			File file = files[i];
			if ( file.isDirectory( ) )
			{ // if file is a directory, get child files and recursively call
				// this method
				File[] dirFiles = file.listFiles( );
				zipFiles( zipOut, dirFiles, tempFolderPath );
			}
			else
			{ // if file is a file, create a new ZipEntry and write out the
				// file.
				BufferedInputStream in = new BufferedInputStream(
						new FileInputStream( file ) );
				String relativePath = generateRelativePath( tempFolderPath,
						file.getPath( ) );
				ZipEntry entry = new ZipEntry( relativePath );
				entry.setTime( file.lastModified( ) );
				zipOut.putNextEntry( entry ); // Create a new zipEntry

				int len;
				byte[] buf = new byte[1024 * 5];
				while ( ( len = in.read( buf ) ) > 0 )
				{
					zipOut.write( buf, 0, len );
				}

				in.close( );
				zipOut.closeEntry( );
			}
		} // end of for ( int i = 0; i < files.length; i++ )
	}

	public static void unzipArchive( File zipArchive, String tempFolderPath )
	{
		try
		{
			ZipFile zipFile = new ZipFile( zipArchive );

			Enumeration entries = zipFile.entries( );
			while ( entries.hasMoreElements( ) )
			{
				ZipEntry entry = (ZipEntry) entries.nextElement( );
				if ( entry.isDirectory( ) )
				{ // Assume directories are stored parents first then
					// children.
					String dirName = generateFullPath( tempFolderPath, entry
							.getName( ) );
					// TODO: handle the error case where the folder can not be
					// created!
					File dir = new File( dirName );
					dir.mkdirs( );
				}
				else
				{
					InputStream in = zipFile.getInputStream( entry );
					File file = new File( generateFullPath( tempFolderPath,
							entry.getName( ) ) );

					File dir = new File( file.getParent( ) );
					if ( dir.exists( ) )
					{
						assert ( dir.isDirectory( ) );
					}
					else
					{
						dir.mkdirs( );
					}

					BufferedOutputStream out = new BufferedOutputStream(
							new FileOutputStream( file ) );

					int len;
					byte[] buf = new byte[1024 * 5];
					while ( ( len = in.read( buf ) ) > 0 )
					{
						out.write( buf, 0, len );
					}
					in.close( );
					out.close( );

				}
			}
			zipFile.close( );

		}
		catch ( ZipException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}

	static public void archive( String folder, String file ) throws IOException
	{
		archive( folder, null, file );
	}

	/**
	 * Compound File Format: <br>
	 * 1long(stream section position) + 1long(entry number in lookup map) +
	 * lookup map section + stream data section <br>
	 * The Lookup map is a hash map. The key is the relative path of the stram.
	 * The entry contains two long number. The first long is the start postion.
	 * The second long is the length of the stream. <br>
	 * 
	 * @param tempFolder
	 * @param fileArchiveName -
	 *            the file archive name
	 * @return Whether the compound file was created successfully.
	 */
	static public void archive( String folderName, IStreamSorter sorter,
			String fileName ) throws IOException
	{
		// Create the file
		ArchiveUtil.DeleteAllFiles( new File( fileName ) ); // Delete existing
															// file or
		// folder that has the same
		// name of the file archive.
		RandomAccessFile compoundFile = new RandomAccessFile( fileName, "rw" ); //$NON-NLS-1$
		try
		{
			archive( folderName, sorter, compoundFile );
		}
		finally
		{
			compoundFile.close( );
		}
	}

	/**
	 * files used to record the reader count reference.
	 */
	static final String READER_COUNT_FILE_NAME = "/.reader.count";
	/**
	 * files which should not be copy into the archives
	 */
	static final String[] SKIP_FILES = new String[]{READER_COUNT_FILE_NAME};

	static boolean needSkip( String file )
	{
		for ( int i = 0; i < SKIP_FILES.length; i++ )
		{
			if ( SKIP_FILES[i].equals( file ) )
			{
				return true;
			}
		}
		return false;
	}

	static void archive( String folderName, IStreamSorter streamSorter,
			RandomAccessFile compoundFile ) throws IOException
	{
		compoundFile.setLength( 0 );
		compoundFile.seek( 0 );

		compoundFile.writeLong( 0 ); // reserve a spot for writing the start
										// position of the stream data section
										// in the file
		compoundFile.writeLong( 0 ); // reserve a sopt for writing the entry
										// number of the lookup map.

		ArrayList fileList = new ArrayList( );
		
		folderName = new File( folderName ).getCanonicalPath( );
		getAllFiles( new File( folderName ), fileList );

		if ( streamSorter != null )
		{
			ArrayList streamNameList = new ArrayList( );
			for ( int i = 0; i < fileList.size( ); i++ )
			{
				File file = (File) fileList.get( i );
				streamNameList.add( ArchiveUtil.generateRelativePath(
						folderName, file.getAbsolutePath( ) ) );
			}

			// Sort the streams by using the stream sorter (if any).
			ArrayList sortedNameList = streamSorter.sortStream( streamNameList ); 

			if ( sortedNameList != null )
			{
				fileList.clear( );
				for ( int i = 0; i < sortedNameList.size( ); i++ )
				{
					String fileName = ArchiveUtil.generateFullPath( folderName,
							(String) sortedNameList.get( i ) );
					fileList.add( new File( fileName ) );
				}
			}
		}

		// Generate the in-memory lookup map and serialize it to the compound
		// file.
		long streamRelativePosition = 0;
		long entryNum = 0;
		for ( int i = 0; i < fileList.size( ); i++ )
		{
			File file = (File) fileList.get( i );
			String relativePath = ArchiveUtil.generateRelativePath( folderName,
					file.getAbsolutePath( ) );
			if ( !needSkip( relativePath ) )
			{
				compoundFile.writeUTF( relativePath );
				compoundFile.writeLong( streamRelativePosition );
				compoundFile.writeLong( file.length( ) );

				streamRelativePosition += file.length( );
				entryNum++;
			}
		}

		// Write the all of the streams to the stream data section in the
		// compound file
		long streamSectionPos = compoundFile.getFilePointer( );
		for ( int i = 0; i < fileList.size( ); i++ )
		{
			File file = (File) fileList.get( i );
			String relativePath = ArchiveUtil.generateRelativePath( folderName,
					file.getAbsolutePath( ) );
			if ( !needSkip( relativePath ) )
			{
				copyFileIntoTheArchive( file, compoundFile );
			}
		}

		// go back and write the start position of the stream data section and
		// the entry number of the lookup map
		compoundFile.seek( 0 );
		compoundFile.writeLong( streamSectionPos );
		compoundFile.writeLong( entryNum );
	}

	/**
	 * Copy files from in to out
	 * 
	 * @param in -
	 *            input file
	 * @param out -
	 *            output compound file. Since the input is only part of the
	 *            file, the compound file output should be be closed by caller.
	 * @throws Exception
	 */
	static private long copyFileIntoTheArchive( File in, RandomAccessFile out )
			throws IOException
	{
		long totalBytesWritten = 0;
		FileInputStream fis = new FileInputStream( in );
		byte[] buf = new byte[1024 * 5];

		int i = 0;
		while ( ( i = fis.read( buf ) ) != -1 )
		{
			out.write( buf, 0, i );
			totalBytesWritten += i;
		}
		fis.close( );

		return totalBytesWritten;
	}

	/**
	 * Get all the files under the specified folder (including all the files
	 * under sub-folders)
	 * 
	 * @param dir -
	 *            the folder to look into
	 * @param fileList -
	 *            the fileList to be returned
	 */
	static void getAllFiles( File dir, ArrayList fileList )
	{
		if ( dir.exists( ) && dir.isDirectory( ) )
		{
			File[] files = dir.listFiles( );
			if ( files == null )
				return;

			for ( int i = 0; i < files.length; i++ )
			{
				File file = files[i];
				if ( file.isFile( ) )
				{
					fileList.add( file );
				}
				else if ( file.isDirectory( ) )
				{
					getAllFiles( file, fileList );
				}
			}
		}
	}

	static public void expand( String file, String folder ) throws IOException
	{
		FileArchiveReader reader = new FileArchiveReader( file );
		try
		{
			reader.open( );
			reader.expandFileArchive( folder );
		}
		finally
		{
			reader.close( );
		}
	}
}