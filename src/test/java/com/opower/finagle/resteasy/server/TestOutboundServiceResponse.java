package com.opower.finagle.resteasy.server;

import com.google.common.collect.ImmutableMap;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;

import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static com.opower.finagle.resteasy.AssertionHelpers.assertContentEquals;

/**
 * Tests for the mapping of Resteasy responses to Netty
 *
 * @author ed.peters
 */
public class TestOutboundServiceResponse {

    private OutboundServiceResponse response =
            new OutboundServiceResponse(HTTP_1_1);

    @Test
    public void testSetStatusAndSetError() throws Exception {
        Map<Status,HttpResponseStatus> statusMap = ImmutableMap.of(
                Status.CONFLICT, HttpResponseStatus.CONFLICT,
                Status.BAD_REQUEST, HttpResponseStatus.BAD_REQUEST,
                Status.FORBIDDEN, HttpResponseStatus.FORBIDDEN
        );
        for (Status from : statusMap.keySet()) {
            this.response.setStatus(from.getStatusCode());
            assertStatusEquals(statusMap.get(from));
        }
        for (Status from : statusMap.keySet()) {
            this.response.sendError(from.getStatusCode());
            assertStatusEquals(statusMap.get(from));
        }
        for (Status from : statusMap.keySet()) {
            String uuid = UUID.randomUUID().toString();
            this.response.sendError(from.getStatusCode(), uuid);
            assertStatusEquals(
                    new HttpResponseStatus(from.getStatusCode(), uuid));
        }
    }

    @Test
    public void testSettingAndClearingHeaders() throws Exception {
        assertEquals(null, this.response.getNettyResponse().getHeader("k1"));
        this.response.getOutputHeaders().putSingle("k1", "v1");
        assertEquals("v1", this.response.getNettyResponse().getHeader("k1"));
        this.response.reset();
        assertEquals(null, this.response.getNettyResponse().getHeader("k1"));
    }

    @Test
    public void testWritingOutput() throws Exception {
        byte [] expectedContent = UUID.randomUUID().toString().getBytes();
        PrintStream out = new PrintStream(this.response.getOutputStream());
        out.write(expectedContent);
        out.flush();
        assertContentEquals(this.response.getNettyResponse().getContent(),
                expectedContent);
    }

    private void assertStatusEquals(HttpResponseStatus expectedStatus) {
        HttpResponseStatus actualStatus =
                this.response.getNettyResponse().getStatus();
        assertNotNull("status is null", actualStatus);
        assertEquals("wrong code",
                expectedStatus.getCode(),
                actualStatus.getCode());
        assertEquals("wrong reason",
                expectedStatus.getReasonPhrase(),
                actualStatus.getReasonPhrase());
    }
}
