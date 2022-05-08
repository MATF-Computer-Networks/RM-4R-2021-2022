package Loto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class LotoServer {

    public static int PORT_SERVER = 12345;

    public static void main(String[] args) {

        int[] combination = new int[7];
        int i = 0;
        Random random = new Random();
        while (i < 7) {
            int num = random.nextInt(39) + 1;
            if (Arrays.stream(combination).anyMatch(u -> u == num)) {
                continue;
            }
            combination[i] = num;
            i++;
        }

        System.out.print("Winning numbers: ");
        Arrays.sort(combination);
        for (int num : combination)
            System.out.print(num + " ");
        System.out.println();

        try (ServerSocketChannel serverChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            if (!serverChannel.isOpen() || !selector.isOpen()) {
                System.err.println("Server channel or selector cannot be opened!");
                System.exit(1);
            }

            serverChannel.bind(new InetSocketAddress(PORT_SERVER));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();

                        SocketChannel client = server.accept();
                        System.out.println("New client accepted!");

                        client.configureBlocking(false);
                        SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);

                        ByteBuffer buffer = ByteBuffer.allocate(7 * 4);
                        clientKey.attach(buffer);

                    } else if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        client.write(buffer);

                        if (!buffer.hasRemaining()) {
                            client.close();
                            System.out.println("Finished!");
                        }


                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        client.read(buffer);

                        if (!buffer.hasRemaining()) {
                            buffer.flip();
                            int result = 0;
                            while (buffer.hasRemaining()) {
                                int num = buffer.getInt();
                                if (Arrays.stream(combination).anyMatch(u -> u == num))
                                    result++;
                            }

                            buffer.clear();
                            buffer.putInt(result);
                            buffer.flip();

                            key.interestOps(SelectionKey.OP_WRITE);
                        }

                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
