package Loto;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Scanner;

public class LotoClient {

    public static void main(String[] args) {

        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", LotoServer.PORT_SERVER));
             Scanner sc = new Scanner(System.in)) {

            //client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(7 * 4);

            int[] combination = new int[7];
            int i = 0;
            while (i < 7) {
                System.out.print("Enter next winning number from [1, 39]: ");
                int num = sc.nextInt();
                if (num < 1 || num > 39) {
                    System.out.println("Number must be in the range [1, 39]!");
                    continue;
                }
                if (Arrays.stream(combination).anyMatch(u -> u == num)) {
                    System.out.println("Already exists!");
                    continue;
                }
                combination[i] = num;
                i++;
            }

            for (int num : combination)
                buffer.putInt(num);

            buffer.flip();
            client.write(buffer);

            buffer.clear();
            client.read(buffer);

            buffer.flip();
            int result = buffer.getInt();

            System.out.println("Result: " + result);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
