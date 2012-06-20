package com.hascode.plugin.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionService;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.zxing.WriterException;
import com.hascode.plugin.components.CodeCreator;

public class QRCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(QRCodeServlet.class);
	private final CodeCreator codeCreator;
	private final PermissionService permissionService;
	private final StashAuthenticationContext authContext;
	private final RepositoryService repoService;
	private final NavBuilder navBuilder;

	public QRCodeServlet(final CodeCreator codeCreator,
			final PermissionService permissionService,
			final StashAuthenticationContext authContext,
			final RepositoryService repoService, final NavBuilder navBuilder) {
		this.codeCreator = codeCreator;
		this.permissionService = permissionService;
		this.authContext = authContext;
		this.repoService = repoService;
		this.navBuilder = navBuilder;
	}

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse res) throws ServletException, IOException {
		final String repository = req.getParameter("r");
		if (StringUtils.isEmpty(repository)) {
			log.warn("no repository given - we're out of here ...");
			signalError(res);
			return;
		}

		final Repository repo = repoService.findRepositoryById(NumberUtils
				.createInteger(repository));
		if (null == repo) {
			log.warn("no repository for given id '{}' found", repository);
			signalError(res);
			return;
		}

		final StashUser user = authContext.getCurrentUser();
		if (null == user
				|| !permissionService.hasRepositoryPermission(user, repo,
						Permission.REPO_READ)) {
			log.warn("user has no permission to view this repository");
			signalError(res);
			return;
		}

		final String url = createUrl(repo);

		try {
			final BufferedImage img = codeCreator.createURLCode(url);
			if (img == null) {
				log.warn("created image was null");
				signalError(res);
				return;
			}

			sendPngImage(res, img);
		} catch (WriterException e) {
			log.warn(
					"a writer exception occurred when trying to create the image",
					e);
		}

	}

	private String createUrl(final Repository repo) {
		return navBuilder.repo(repo).buildAbsolute();
	}

	private void signalError(final HttpServletResponse res) {
		log.debug("sending error status");
		res.setStatus(404);
	}

	private void sendPngImage(final HttpServletResponse res,
			final BufferedImage image) throws IOException {
		res.setContentType("image/png");
		ImageIO.write(image, "PNG", res.getOutputStream());
	}

}